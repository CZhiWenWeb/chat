package com.chat.cache.message;

/**
 * @Author: czw
 * @CreateTime: 2020-06-11 15:04
 * @UpdeteTime: 2020-06-11 15:04
 * @Description:
 */
public class RingBufferFlip {

	public byte[] elements = null;
	private int capacity = 0;
	private int writePos = 0;
	private int readPos = 0;
	//读写标记交换位置
	private boolean flipped = false;

	public RingBufferFlip(int capacity) {
		this.capacity = capacity;
		this.elements = new byte[capacity];
	}

	public void reset() {
		this.writePos = 0;
		this.readPos = 0;
		this.flipped = false;
	}

	public int capacity() {
		return this.capacity;
	}

	public int available() {
		if (!flipped) {     //未交换可用元素数量
			return writePos - readPos;
		}
		return capacity - readPos + writePos;
	}

	public int remainCapacity() {
		if (!flipped) {
			return capacity - writePos;
		}
		return readPos - writePos;
	}

	public boolean put(byte element) {
		if (!flipped) { //未交换时
			if (writePos == capacity) { //写入的数量等于容量时,交换
				writePos = 0;
				flipped = true;

				if (writePos < readPos) {
					elements[writePos++] = element;
					return true;
				} else {
					return false;
				}
			} else {
				elements[writePos++] = element;     //未写满，继续写入
				return true;
			}
		} else {     //已交换时
			if (writePos < readPos) {
				elements[writePos++] = element;
				return true;
			} else {
				return false;
			}
		}
	}

	public byte take() {
		if (!flipped) {
			if (readPos < writePos) {
				return elements[readPos++];
			} else {
				return Byte.parseByte(null);
			}
		} else {
			if (readPos == capacity) {
				readPos = 0;
				flipped = false;
				if (readPos < writePos) {
					return elements[readPos++];
				} else {
					return Byte.parseByte(null);
				}
			} else {
				return elements[readPos++];
			}
		}
	}

}
