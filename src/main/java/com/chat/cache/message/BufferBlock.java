package com.chat.cache.message;

/**
 * @Author: czw
 * @CreateTime: 2020-06-12 16:24
 * @UpdeteTime: 2020-06-12 16:24
 * @Description:
 */
public class BufferBlock {
	byte[] buffer;
	int writePos;
	int readPos;
	int capacity;
	int mark;
	AllocateBuffer bufferPool;

	public BufferBlock(byte[] bf, int mark, int capacity, AllocateBuffer bufferPool) {
		this.buffer = bf;
		this.writePos = mark * capacity;
		this.capacity = capacity;
		this.readPos = writePos;
		this.mark = mark;
		this.bufferPool = bufferPool;
	}

	public int writeLen() {
		return writePos - mark * capacity;
	}

}
