package com.chat.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-13 11:18
 * @UpdeteTime: 2020-06-13 11:18
 * @Description:
 */
public class SocketAccepter implements Runnable {
	//待处理的socketChannel队列
	Queue<Socket> queue = new LinkedBlockingQueue<>();
	private ServerSocketChannel ssc;
	private boolean stop;

	public SocketAccepter(int port) {
		SocketAddress address = new InetSocketAddress(port);
		try {
			ssc = ServerSocketChannel.open().bind(address);
			ssc.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (!stop) {
				SocketChannel sc = ssc.accept();

				System.out.println("Socket accepted:" + sc);

				queue.offer(new Socket(sc));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
