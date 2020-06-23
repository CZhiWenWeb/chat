package com.chat.socket;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:51
 * @UpdeteTime: 2020-06-17 10:51
 * @Description:
 */
public class WriterProcessor implements Runnable {
	static AtomicInteger integer = new AtomicInteger();
	private Queue<SocketReader> outbound;
	public static Map map = ReaderProcessor.map;
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
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlerMsg() throws IOException {
		SocketReader socketReader = outbound.poll();
		while (socketReader != null) {
			sendMsg(socketReader);
			//ByteBuffer byteBuffer = socketReader.byteBuffer;
			//byteBuffer.put((socketReader.socketId + ":hello").getBytes());
			//byteBuffer.flip();
			//socketReader.sc.write(byteBuffer);
			//byteBuffer.clear();
			socketReader = outbound.poll();
		}
	}

	private void sendMsg(SocketReader socketReader) throws IOException {
		Set<Integer> msgIds = socketReader.msgMap.keySet();  //需发送的msg
		for (Integer msgId : msgIds) {
			Queue<Long> toClientIds = socketReader.msgToIdsMap.get(msgId);
			Long temp = toClientIds.poll();
			while (temp != null) {
				SocketReader toSocket = (SocketReader) map.get(temp);
				if (toSocket != null && toSocket.sc.isOpen()) {
					byte[] bytes = socketReader.msgMap.get(msgId);
					toSocket.writeBuffer.put(bytes);
					String end = " from:" + socketReader.socketId;
					toSocket.writeBuffer.put(end.getBytes());   //后缀必然小于header,不必校验cap
					toSocket.writeBuffer.flip();
					int i = toSocket.sc.write(toSocket.writeBuffer);
					assert i == bytes.length + end.getBytes().length;   //断言发送成功
					integer.incrementAndGet();
					toSocket.writeBuffer.clear();
					System.out.println(new String(bytes) + end + " :sendSuccess  累计成功：" + integer.intValue());

				}
				temp = toClientIds.poll();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			socketReader.msgToIdsMap.clear();
		}
		msgIds.clear();
	}


}
