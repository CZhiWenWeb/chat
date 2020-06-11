package com.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 16:00
 * @UpdeteTime: 2020-06-08 16:00
 * @Description:
 */
public class ChatClient extends Frame implements Runnable {
	//并发测试
	static CountDownLatch count = new CountDownLatch(100);

	public ChatClient() {
		//初始化窗口
		initFrame(10, 10, 10, 10);
		//初始化连接
		initNetWork();
		//分配缓存
		bf = ByteBuffer.allocate(1024);
		if (!openWindow) {
			//只有一台机器使用系统时间为id
			synchronized (ChatClient.class) {
				name = String.valueOf(System.currentTimeMillis());
			}
		}
	}

	static final boolean openWindow = false;
	static final String nextLine = "\r\n";
	private boolean stop;
	private String ip = "127.0.0.1";
	private int port = 12346;
	private Selector selector;
	private SocketChannel sc;
	private TextField tfField;
	private TextArea taContent;
	private ByteBuffer bf;
	private String name;
	private void initFrame(int x, int y, int w, int h) {
		this.tfField = new TextField();
		this.taContent = new TextArea();
		this.setBounds(x, y, w, h);
		this.setLayout(new BorderLayout());
		//添加窗口关闭监听
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//结束线程
				stop = true;
				//关闭窗口
				setVisible(false);
				System.exit(0);
			}
		});
		this.taContent.setEditable(false);
		this.add(tfField, BorderLayout.SOUTH);
		this.add(taContent, BorderLayout.NORTH);
		//添加回车监听
		this.tfField.addActionListener(actionE -> {
			String str = tfField.getText().trim();
			send(str);
			taContent.append(str + nextLine);
		});
		this.pack();
		//是否打开窗口
		this.setVisible(openWindow);
	}

	private void login() {
		String name;
		String pw;
		if (openWindow) {
			name = JOptionPane.showInputDialog("name");
			pw = JOptionPane.showInputDialog("pw");
		} else {
			name = this.name;
			pw = "sss";
		}
		try {
			while (!sc.finishConnect()) {
				Thread.sleep(100);
			}
		} catch (InterruptedException ignored) {
		} catch (IOException e) {
			System.out.println("socketChannel连接失败");
			e.printStackTrace();
		}
		send("login:" + name + ":" + pw);
	}

	private void initNetWork() {
		try {
			//打开管道
			SocketChannel sc = SocketChannel.open();
			//设置非阻塞
			sc.configureBlocking(false);
			this.sc = sc;
			//打开就绪事件选择器
			Selector selector = Selector.open();
			this.selector = selector;
			//注册感兴趣事件为content和read
			sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			System.out.println("initNetWork失败");
			try {
				if (sc.isOpen())
					sc.close();
				if (selector.isOpen())
					selector.close();
			} catch (IOException ignored) {
			}
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		try {
			//count.await();
			//发起连接
			sc.connect(new InetSocketAddress(ip, port));
			while (!stop) {
				int nums = selector.select();
				Set<SelectionKey> keys;
				if (nums > 0) {
					keys = selector.selectedKeys();
					for (SelectionKey key : keys)
						handlerKey(key);
					keys.clear();
				}
			}
		} catch (IOException e) {
			System.out.println("发起连接失败");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handlerKey(SelectionKey key) {
		if (key.isValid()) {
			if (key.isConnectable()) {
				//连接就绪后登入
				login();
			}
			if (key.isReadable()) {
				bf.clear();
				try {
					sc.read(bf);
					bf.flip();
					byte[] bytes = new byte[bf.remaining()];
					bf.get(bytes);
					String temp = new String(bytes);
					if (openWindow)
						taContent.append(temp + nextLine);
					else
						System.out.println(temp);
				} catch (IOException e) {
					System.out.println("读取失败");
					try {
						sc.close();
					} catch (IOException ignored) {
					}
					e.printStackTrace();
				}
			}
		}
	}

	private void send(String message) {
		System.out.println("client send:" + message);
		bf.clear();
		bf.put(message.getBytes());
		bf.flip();
		try {
			sc.write(bf);
		} catch (IOException e) {
			System.out.println("ChatClient  send()异常");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		for (int i = 0; i < 100; i++) {
			ChatClient cc = new ChatClient();
			Thread t = new Thread(cc);
			t.start();
			count.countDown();
		}
	}
}
