package com.chat.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:50
 * @UpdeteTime: 2020-06-17 10:50
 * @Description:
 */
public class ReaderProcessor implements Runnable {
	private Queue<SocketChannel> inboundSocket;
	public Queue<SocketReader> outboundSocket;
	private boolean stop;
	private Selector readSelector;
	public static Map<Long, SocketReader> map = new HashMap<>();

	public ReaderProcessor(Queue<SocketChannel> queue) {
		this.inboundSocket = queue;
		this.outboundSocket = new LinkedBlockingQueue<>();
	}

	@Override
	public void run() {
		try {
			readSelector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (!stop) {
			try {
				handlerSocketChannel();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlerSocketChannel() throws IOException {
		SocketChannel sc = inboundSocket.poll();

		while (sc != null) {
			sc.configureBlocking(false);
			sc.register(readSelector, SelectionKey.OP_READ)
					.attach(new SocketReader(sc));
			sc = inboundSocket.poll();
		}

		int nums = readSelector.selectNow();
		if (nums > 0) {
			Set<SelectionKey> keys = readSelector.selectedKeys();
			for (SelectionKey key : keys) {
				SocketReader reader = (SocketReader) key.attachment();
				reader.read();

				outboundSocket.offer(reader);
			}
			keys.clear();
		}
	}
}
