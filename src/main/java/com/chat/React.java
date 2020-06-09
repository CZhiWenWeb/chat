package com.chat;

import com.chat.cache.util.ICacheTool;
import com.chat.cache.util.RedisClientTool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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
		private ByteBuffer readBf;
		private ByteBuffer writeBf;

		public ReadHandler(Selector selector, SocketChannel sc) throws IOException {
			this.sc = sc;
			sc.configureBlocking(false);
			SelectionKey key = sc.register(selector, SelectionKey.OP_READ);
			key.attach(this);
		}

		@Override
		public void run() {
			try {
				readBf = ByteBuffer.allocate(1042);
				sc.read(readBf);
				readBf.flip();
				byte[] bytes = new byte[readBf.remaining()];
				readBf.get(bytes);
				handMessage(new String(bytes));
			} catch (IOException e) {
				e.printStackTrace();
				try {
					sc.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}

		private void handMessage(String message) throws IOException {
			if (message.contains("login")) {
				String[] strings = message.substring("login".length() + 1).split(":");
				Person person = new Person(strings[0], strings[1]);
				ICacheTool cacheTool = new RedisClientTool();
				cacheTool.set(strings[0], person, -1);
				send("login success");
			} else {

			}
		}

		private void send(String message) throws IOException {
			writeBf = ByteBuffer.allocate(1024);
			writeBf.put(message.getBytes());
			writeBf.flip();
			sc.write(writeBf);
			sc.close();
		}
	}

	public static void main(String[] args) throws IOException {
		React react = new React(12346);
		Thread thread = new Thread(react);
		thread.start();
	}
}
