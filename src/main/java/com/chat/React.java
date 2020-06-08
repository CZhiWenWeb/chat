package com.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 09:37
 * @UpdeteTime: 2020-06-08 09:37
 * @Description:
 */
public class React implements Runnable {
	private ServerSocketChannel ssc;

	private Selector selector;

	public React(int port) throws IOException {
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
				SocketChannel sc = ssc.accept();
				new ReadHandler(selector, sc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class ReadHandler implements Runnable {
		private SocketChannel sc;
		private ByteBuffer readBf = ByteBuffer.allocate(1024);
		private ByteBuffer writeBf = ByteBuffer.allocate(1024);

		public ReadHandler(Selector selector, SocketChannel sc) throws IOException {
			this.sc = sc;
			sc.configureBlocking(false);
			SelectionKey key = sc.register(selector, SelectionKey.OP_READ);
			key.attach(this);
		}

		@Override
		public void run() {
			try {
				sc.read(readBf);
				readBf.flip();
				System.out.println();
				new WriteHandler(selector, sc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		class WriteHandler implements Runnable {
			String httpResponse = "HTTP/1.1 200 OK\r\n" +
					"Content-Type:text/html\r\n" +
					"Connection:keep-alive\r\n" +
					//允许跨域
					"Access-Control-Allow-Origin:*\r\n" +
					"Cache-Control:no-cache\r\n" +
					"\r\n" +
					"data:hello world";
			SelectionKey key;

			WriteHandler(Selector selector, SocketChannel sc) throws ClosedChannelException {
				key = sc.register(selector, SelectionKey.OP_WRITE);
				key.attach(this);
			}

			@Override
			public void run() {
				writeBf.put(httpResponse.getBytes());
				writeBf.flip();
				try {
					sc.write(writeBf);
					key.cancel();
					sc.close();
				} catch (IOException e) {
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
