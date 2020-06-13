package com.chat.cache.message;

import java.nio.ByteBuffer;

/**
 * @Author: czw
 * @CreateTime: 2020-06-13 08:41
 * @UpdeteTime: 2020-06-13 08:41
 * @Description:
 */
public class Message {
	public BufferBlock messageBf;

	public long socketId;

	public int startMark = -1;

	public Message(BufferBlock bufferBlock) {
		this.messageBf = bufferBlock;
	}

	public int writeToMessage(ByteBuffer byteBuffer) {
		int remaining = byteBuffer.remaining();

		while (messageBf.writeLen() + remaining > messageBf.capacity) {
			//扩容，发放一个新的BufferBlock
			messageBf = messageBf.bufferPool.expand(messageBf);
		}
		//写入缓冲数组
		byteBuffer.get(messageBf.buffer,
				messageBf.writePos,
				remaining);
		//更新写指针位置
		messageBf.writePos += remaining;

		return remaining;
	}

	public int writeToMessage(byte[] bytes, int offset, int len) {
		while (messageBf.writeLen() + len > messageBf.capacity) {
			messageBf = messageBf.bufferPool.expand(messageBf);
		}

		System.arraycopy(bytes, offset,
				messageBf.buffer, messageBf.writePos,
				len);

		return len;
	}
}
