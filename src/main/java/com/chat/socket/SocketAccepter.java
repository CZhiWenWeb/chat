package com.chat.socket;

import com.chat.util.CheckSocketAlive;

import java.io.Closeable;
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
public class SocketAccepter implements Runnable, Closeable {
	public Queue<SocketChannel> socketQue = new LinkedBlockingQueue<>();
	private ServerSocketChannel ssc;
	private boolean stop;
	private SocketAddress address;

	public SocketAccepter(int port) {
		SocketAddress address = new InetSocketAddress(port);
		this.address = address;
	}

	@Override
	public void run() {
		try {
			//使用阻塞模式
			ssc = ServerSocketChannel.open().bind(address);

		} catch (IOException e) {
			e.printStackTrace();
		}
		//在线检测
		CheckSocketAlive.start();
		try {
			while (!stop) {
				SocketChannel sc = ssc.accept();

				System.out.println("Socket accepted:" + sc);

				socketQue.offer(sc);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException {
		stop = true;
		ssc.close();
	}
}
