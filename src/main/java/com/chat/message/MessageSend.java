package com.chat.message;

import com.chat.cache.BufferBlock;

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

	public void sendMsg(Message msg) throws IOException {
		sendMsg(msg.getMsg());
	}

	public void sendMsg(BufferBlock bufferBlock) throws IOException {
		while (bufferBlock.readCap() > 0) {
			int num = cap > bufferBlock.readCap() ? bufferBlock.readCap() : cap;
			byteBuffer.put(bufferBlock.bytes, bufferBlock.readOff, num);
			byteBuffer.flip();
			int i = sc.write(byteBuffer);
			byteBuffer.clear();
			bufferBlock.readOff += i;
		}
	}

}
