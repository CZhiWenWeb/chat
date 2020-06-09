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

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 16:00
 * @UpdeteTime: 2020-06-08 16:00
 * @Description:
 */
public class ChatClient extends Frame implements Runnable {
	public ChatClient() throws IOException {
		//初始化窗口
		initFrame(10, 10, 10, 10);
		//初始化连接
		initNetWork();
	}

	private boolean stop;
	private String ip = "127.0.0.1";
	private int port = 12346;
	private Selector selector;
	private SocketChannel sc;
	private SelectionKey contentKey;
	private TextField tfField;
	private TextArea taContent;
	private ByteBuffer bf;
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
			try {
				send(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		this.pack();
		this.setVisible(true);
	}

	private void login() throws InterruptedException, IOException {
		String name = JOptionPane.showInputDialog("name");
		String pw = JOptionPane.showInputDialog("pw");
		while (!sc.finishConnect()) {
			Thread.sleep(1000);
		}
		send("login:" + name + ":" + pw);
	}

	private void initNetWork() throws IOException {
		//打开管道
		SocketChannel sc = SocketChannel.open();
		//设置非阻塞
		sc.configureBlocking(false);
		this.sc = sc;
		//打开就绪事件选择器
		Selector selector = Selector.open();
		this.selector = selector;
		//注册感兴趣事件为content
		SelectionKey key = sc.register(selector, SelectionKey.OP_READ);
	}

	private void send(String message) throws IOException {
		bf = ByteBuffer.allocate(1024);
		bf.put(message.getBytes());
		bf.flip();
		sc.write(bf);
	}

	@Override
	public void run() {

		try {
			//发起连接
			sc.connect(new InetSocketAddress(ip, port));
			int nums = selector.select();
			while (!stop) {
				Set<SelectionKey> keys;
				if (nums > 0) {
					keys = selector.selectedKeys();
					for (SelectionKey key : keys)
						//分发事件
						dispatch(key);
					keys.clear();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	void dispatch(SelectionKey key) {

	}

	class ContentHandler implements Runnable {

		@Override
		public void run() {
			try {

				login();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) throws IOException {
		ChatClient cc = new ChatClient();
		cc.run();
	}

}
