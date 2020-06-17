package com.chat.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:51
 * @UpdeteTime: 2020-06-17 10:51
 * @Description:
 */
public class WriterProcessor implements Runnable {
	private Queue<SocketReader> outbound;

	public WriterProcessor(Queue<SocketReader> queue) {
		outbound = queue;
	}

	@Override
	public void run() {
		while (true) {
			try {
				handlerMsg();
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

	private void handlerMsg() throws IOException {
		SocketReader socketReader = outbound.poll();
		while (socketReader != null) {
			ByteBuffer byteBuffer = socketReader.byteBuffer;
			byteBuffer.put((socketReader.socketId + ":hello").getBytes());
			byteBuffer.flip();
			socketReader.sc.write(byteBuffer);
			byteBuffer.clear();
			socketReader = outbound.poll();
		}
	}

}
