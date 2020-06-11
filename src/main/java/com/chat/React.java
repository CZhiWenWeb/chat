package com.chat;

import com.chat.cache.util.ICacheTool;
import com.chat.cache.util.RedisClientTool;
import com.chat.cache.util.ScheduledUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 09:37
 * @UpdeteTime: 2020-06-08 09:37
 * @Description:
 */
public class React implements Runnable {
	private ServerSocketChannel ssc;

	private Selector selector;

	private static Map<String, SocketChannel> socketChannelMap = new HashMap<>();
	private static AtomicInteger userOnLine = new AtomicInteger();

	public React(int port) {
		try {
			//打开ssc管道
			ssc = ServerSocketChannel.open()
					.bind(new InetSocketAddress(port));
			//设置非阻塞
			ssc.configureBlocking(false);
			//打开选择器，负责监听文件就绪状态，这里是socket
			selector = Selector.open();
			//设置感兴趣事件为接收
			SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);
			//为事件绑定回调函数
			key.attach(new Acceptor());

			Runnable printUserOnLine = () -> {

				System.out.println(userOnLine.intValue());
			};
			//定时打印在线人数
			ScheduledUtil.start(printUserOnLine, 0, 30);
		} catch (Exception e) {
			System.out.println("reactServer初始化失败");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Set<SelectionKey> setKeys;
		while (!Thread.interrupted()) {
			try {
				int nums = selector.select();
				if (nums > 0) {
					setKeys = selector.selectedKeys();
					for (SelectionKey key : setKeys)
						dispatch(key);
					setKeys.clear();

				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void dispatch(SelectionKey key) {
		Runnable r = (Runnable) key.attachment();
		if (r != null)
			r.run();
	}

	//接收事件回调处理器
	class Acceptor implements Runnable {

		@Override
		public void run() {
			try {
				System.out.println("acceptor");
				SocketChannel sc = ssc.accept();
				new Handler(selector, sc);
			} catch (IOException e) {
				System.out.println("socketChannel连接失败");
				e.printStackTrace();
			}
		}
	}

	static class Handler implements Runnable {
		private SocketChannel sc;
		private ByteBuffer readBf;
		private ByteBuffer writeBf;
		static ExecutorService es;
		static int num = 0;
		private SelectionKey key;

		static {
			es = Executors.newFixedThreadPool(5, r -> {
				System.out.println("新建线程" + num++);
				return new Thread(r);
			});
		}

		public Handler(Selector selector, SocketChannel sc) {
			try {
				this.sc = sc;
				sc.configureBlocking(false);
				key = sc.register(selector, SelectionKey.OP_READ);
				key.attach(this);
				readBf = ByteBuffer.allocate(1024);
				writeBf = ByteBuffer.allocate(1024);
			} catch (IOException e) {
				System.out.println("Handler初始化失败");
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			es.submit(new Task());
		}

		class Task implements Runnable {

			@Override
			public void run() {
				try {
					readBf.clear();
					sc.read(readBf);
					readBf.flip();
					byte[] bytes = new byte[readBf.remaining()];
					readBf.get(bytes);
					handMessage(new String(bytes));
				} catch (IOException e) {
					System.out.println("reactServer读取数据失败");
					e.printStackTrace();
					try {
						sc.close();
					} catch (IOException ignored) {
					}
				}
			}

			private void handMessage(String message) {
				String name = Thread.currentThread().getName();
				if (message.contains("login")) {
					String[] strings = message.substring("login:".length()).split(":");
					Person person = new Person(strings[0], strings[1]);
					ICacheTool cacheTool = new RedisClientTool();
					cacheTool.set(strings[0], person, -1);
					socketChannelMap.put(strings[0], sc);
					send(sc, strings[0] + "   login success：by_" + name);
					//记录在线人数
					userOnLine.incrementAndGet();
				} else if (message.contains("everyone")) {
					String s = message.substring("everyone:".length());
					socketChannelMap.forEach((key, val) -> {
						if (val != null && val.isOpen())
							send(val, "everyone:" + s + "    from:" + key + "by:" + name);
					});
				} else {
					String[] strings = message.split(":");
					if (message.equals("")) {
						//System.out.println("心跳连接："+sc.socket()+"  "+key);
						return;
					}
					SocketChannel socketChannel = socketChannelMap.get(strings[0]);
					if (socketChannel != null && socketChannel.isOpen()) {
						send(socketChannel, strings[1]);
					} else {
						send(sc, strings[0] + "已下线" + " by:" + name);
					}
				}
			}

			private void send(SocketChannel sc, String message) {
				writeBf.clear();
				writeBf.put(message.getBytes());
				writeBf.flip();
				try {
					sc.write(writeBf);
				} catch (IOException e) {
					System.out.println("server send()失败");
					try {
						sc.close();
					} catch (IOException ignored) {
					}
					e.printStackTrace();
				}
			}
		}
	}


	public static void main(String[] args) throws IOException {
		React react = new React(12346);
		Thread thread = new Thread(react);
		thread.start();
	}
}
