package com.chat.socket;

import com.chat.message.MessageAccept;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:50
 * @UpdeteTime: 2020-06-17 10:50
 * @Description:
 */
public class ReaderProcessor implements Runnable {
	private BlockingQueue<SocketChannel> inboundSocket;
	public BlockingQueue outboundSocket;
	private boolean stop;
	private Selector readSelector;

	public ReaderProcessor(BlockingQueue<SocketChannel> queue, BlockingQueue readerQueue) {
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
					.attach(new MessageAccept(sc));
			sc = inboundSocket.poll();
		}

		int nums = readSelector.selectNow();
		if (nums > 0) {
			Set<SelectionKey> keys = readSelector.selectedKeys();
			for (SelectionKey key : keys) {
				MessageAccept accept = (MessageAccept) key.attachment();
				accept.read();

				outboundSocket.offer(accept);   //每次读取完成都向写线程提供一个accept，一个accept每次read后都会重新入列，所以会出现多个写线程同时处理一个accept实例
			}
			keys.clear();
		}
	}
}
