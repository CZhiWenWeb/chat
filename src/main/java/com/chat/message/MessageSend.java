package com.chat.message;

import com.chat.cache.BufferBlockProxy;

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

	public void sendMsg(BufferBlockProxy proxy) throws IOException {
		while (proxy.readCap() > 0) {
			int num = cap > proxy.readCap() ? proxy.readCap() : cap;
			proxy.writeToBuffer(byteBuffer, num);
			byteBuffer.flip();
			int i = sc.write(byteBuffer);
			byteBuffer.clear();
			proxy.readOffLeftShift(num - i);    //发送数据小于读取数据，重新发送
		}
		proxy.clear();
	}

}
