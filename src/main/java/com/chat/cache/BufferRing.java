package com.chat.cache;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-26 20:11
 * @UpdeteTime: 2020-06-26 20:11
 * @Description:
 */
public class BufferRing {
	private final static int KB = 1024;
	private final static int MB = KB * 1024;
	private final static double percent = 0.8;
	private static int capacity = 10 * MB;
	private byte[] primary = new byte[(int) (capacity * percent)];
	private byte[] surviorF = new byte[(int) (capacity * (1 - percent) / 2)];
	private byte[] surviorS = new byte[surviorF.length];
	public static Queue bufferBlockQue = new LinkedList();
	public MessageBuffer primaryBf;
	public MessageBuffer surviorFBf;
	public MessageBuffer surviorSBf;
	public boolean isF;     //空闲内存标记

	public static BufferRing getInstance() {
		return Holder.bufferRing;
	}

	private BufferRing() {
		this.primaryBf = new MessageBuffer(primary);
		this.surviorFBf = new MessageBuffer(surviorF);
		this.surviorSBf = new MessageBuffer(surviorS);
	}

	public synchronized BufferBlock dispatcher(int count) {
		if (isGC(count)) {
			MessageBuffer survior = isF ? surviorFBf : surviorSBf;  //isF为true时有效数据复制至surviorFBf
			int num = bufferBlockQue.size();
			while (num > 0) {
				BufferBlock bufferBlock = (BufferBlock) bufferBlockQue.poll();
				if (bufferBlock != null && bufferBlock.alive) {
					try {
						BufferBlock newBB = survior.dispatcher(bufferBlock.offset - bufferBlock.readOff);
						bufferBlock.copyTo(newBB);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				num--;
			}
			primaryBf.reset();
			if (isF) {
				surviorSBf.reset();
			} else {
				surviorFBf.reset();
			}
			isF = !isF;
			System.out.println("GC成功");
			return dispatcher(count);
		} else {
			try {
				return primaryBf.dispatcher(count);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean isGC(int count) {
		if (primaryBf.capacity - primaryBf.writePos < count) {
			return true;
		}
		return false;
	}

	class MessageBuffer {
		byte[] bytes;   //连续内存
		int capacity;   //最大可用内存
		int writePos;   //写指针位置

		public MessageBuffer(byte[] bytes) {
			this.bytes = bytes;
			this.capacity = bytes.length;
			this.writePos = 0;
		}

		public BufferBlock dispatcher(int count) throws Exception {
			if (capacity - writePos > count) {
				BufferBlock bufferBlock = new BufferBlock(bytes, writePos, count);
				writePos += count;
				return bufferBlock;
			} else {
				throw new Exception("内存不足");
			}
		}

		public void reset() {
			writePos = 0;
		}
	}

	static class Holder {
		static BufferRing bufferRing = new BufferRing();
	}

	public static void main(String[] args) {
		int cap = 500;
		BufferRing.capacity = 10 * KB;
		BufferRing bufferRing = new BufferRing();
		bufferRing.dispatcher(cap);
		for (int i = 0; i < 17; i++) {
			bufferRing.dispatcher(cap).alive = false;
		}
	}
}
