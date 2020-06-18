package com.chat.socket;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:51
 * @UpdeteTime: 2020-06-17 10:51
 * @Description:
 */
public class WriterProcessor implements Runnable {
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
				Thread.sleep(1000);
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
				if (toSocket.sc.isOpen()) {
					toSocket.byteBuffer.put(socketReader.msgMap.get(msgId));
					String end = "\r\nfrom:" + socketReader.socketId;
					toSocket.byteBuffer.put(end.getBytes());
					toSocket.byteBuffer.flip();
					toSocket.sc.write(toSocket.byteBuffer);
					toSocket.byteBuffer.clear();
				}
				temp = toClientIds.poll();
			}
			socketReader.msgToIdsMap.clear();
		}
		msgIds.clear();
	}


}
