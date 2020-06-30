package com.chat.cache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: czw
 * @CreateTime: 2020-06-30 15:20
 * @UpdeteTime: 2020-06-30 15:20
 * @Description:
 */
public class MessageBuffer {
	byte[] bytes;   //连续内存
	AtomicInteger capacity;   //最大可用内存
	AtomicInteger writePos;   //写指针位置

	public MessageBuffer(byte[] bytes) {
		this.bytes = bytes;
		this.capacity = new AtomicInteger(bytes.length);
		this.writePos = new AtomicInteger(0);
	}

	public NewBufferBlock dispatcher(int count) throws Exception {
		if (capacity.intValue() - writePos.intValue() > count) {
			NewBufferBlock bufferBlock = new NewBufferBlock(bytes, writePos.intValue(), count);
			writePos.getAndAdd(count);
			return bufferBlock;
		} else {
			throw new Exception("内存不足：剩余" + (capacity.intValue() - writePos.intValue()));
		}
	}

	public void reset() {
		writePos.set(0);
	}
}
