package com.chat.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Author: czw
 * @CreateTime: 2020-06-23 14:09
 * @UpdeteTime: 2020-06-23 14:09
 * @Description:
 */
public class MessageSend {
	public final static int cap = 1024;
	private SocketChannel sc;
	private ByteBuffer byteBuffer;

	public MessageSend(SocketChannel sc) {
		this.sc = sc;
		byteBuffer = ByteBuffer.allocate(cap);
	}

	public void sendMsg(byte[] bytes) throws IOException {
		int len = bytes.length;   //需要发送的数量
		int start = 0;          //偏移量
		int sendNum = 0;        //成功发送的数量
		while (len > sendNum) {
			len -= sendNum;
			if (len > cap)
				byteBuffer.put(bytes, start, cap);
			else
				byteBuffer.put(bytes, start, len);
			byteBuffer.flip();
			int i = sc.write(byteBuffer);
			byteBuffer.clear();
			start += i;
			sendNum = i;
		}
	}

	public void sendMsg(Message msg) throws IOException {
		sendMsg(msg.getMsg());
	}

}
