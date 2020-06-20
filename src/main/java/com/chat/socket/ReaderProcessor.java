package com.chat.socket;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
	public static Map map = new HashMap();

	public ReaderProcessor(Queue<SocketChannel> queue, Queue<SocketReader> readerQueue) {
		this.inboundSocket = queue;
		this.outboundSocket = readerQueue;
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
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlerSocketChannel() throws Exception {
		SocketChannel sc = inboundSocket.poll();

		while (sc != null) {
			sc.configureBlocking(false);
			sc.register(readSelector, SelectionKey.OP_READ)
					.attach(new SocketReader(sc, readSelector));
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
