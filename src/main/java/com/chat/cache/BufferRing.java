package com.chat.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: czw
 * @CreateTime: 2020-06-26 20:11
 * @UpdeteTime: 2020-06-26 20:11
 * @Description:
 */
public class BufferRing {
	private final static int KB = 1024;
	private final static int MB = KB * 1024;
	private final static double percent = 0.5;
	private static int capacity = 100 * KB;
	static final int maxAges = 4;      //最多经历ags次GC
	private byte[] primary = new byte[(int) (capacity * percent)];
	private byte[] surviorF = new byte[(int) (capacity * (1 - percent))];
	//private byte[] surviorS = new byte[surviorF.length];
	public static BlockingQueue bufferBlockQue = new LinkedBlockingQueue();

	public MessageBuffer primaryBf;
	public MessageBuffer surviorFBf;
	//public MessageBuffer surviorSBf;
	public volatile boolean isF = true;     //空闲内存标记

	public static BufferRing getInstance() {
		return Holder.bufferRing;
	}

	private BufferRing() {
		this.primaryBf = new MessageBuffer(primary);
		this.surviorFBf = new MessageBuffer(surviorF);
		//this.surviorSBf = new MessageBuffer(surviorS);
	}

	public synchronized BufferBlock dispatcher(int count) throws Exception {
		if (isGC(count)) {
			int temp = 0;
			MessageBuffer survior = isF ? surviorFBf : primaryBf;  //isF为true时有效数据复制至surviorFBf
			BlockingQueue oldQue = bufferBlockQue;
			bufferBlockQue = new LinkedBlockingQueue();
			BufferBlock bufferBlock = (BufferBlock) oldQue.poll();
			while (bufferBlock != null) {
				if (bufferBlock.age > maxAges) {
					bufferBlock.clear();    //超过次数强制GC
				}
				if (bufferBlock.alive) {
					try {
						temp += bufferBlock.readCap();
						BufferBlock newBB = survior.dispatcher(bufferBlock.readCap());
						bufferBlock.copyTo(newBB);
					} catch (Exception e) {
						System.out.println("分配失败");
						e.printStackTrace();
					}
				}
				bufferBlock = (BufferBlock) oldQue.poll();
			}
			if (isF) {
				primaryBf.reset();
			} else {
				surviorFBf.reset();
			}
			isF = !isF;
			System.out.println("GC成功:" + temp + "存活");
			return dispatcher(count);
		}
		if (isF) {
			return primaryBf.dispatcher(count);
		} else {
			return surviorFBf.dispatcher(count);
		}
	}

	private boolean isGC(int count) {
		MessageBuffer buffer = isF ? primaryBf : surviorFBf;
		if (buffer.capacity.intValue() - buffer.writePos.intValue() < count) {
			return true;
		}
		return false;
	}

	class MessageBuffer {
		byte[] bytes;   //连续内存
		AtomicInteger capacity;   //最大可用内存
		AtomicInteger writePos;   //写指针位置

		public MessageBuffer(byte[] bytes) {
			this.bytes = bytes;
			this.capacity = new AtomicInteger(bytes.length);
			this.writePos = new AtomicInteger(0);
		}

		public BufferBlock dispatcher(int count) throws Exception {
			if (capacity.intValue() - writePos.intValue() > count) {
				BufferBlock bufferBlock = new BufferBlock(bytes, writePos.intValue(), count);
				writePos.getAndAdd(count);
				return bufferBlock;
			} else {
				System.out.println(this.writePos);
				throw new Exception("内存不足");
			}
		}

		public void reset() {
			writePos.set(0);
		}
	}

	static class Holder {
		static BufferRing bufferRing = new BufferRing();
	}

	public static void main(String[] args) throws Exception {
		int cap = 500;
		BufferRing.capacity = 10 * KB;
		BufferRing bufferRing = new BufferRing();
		bufferRing.dispatcher(cap);
		for (int i = 0; i < 17; i++) {
			bufferRing.dispatcher(cap).clear();
		}
	}
}
