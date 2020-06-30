package com.chat.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
	private static int capacity = 4 * MB;   //当不发生大数量强制GC时为合适容量
	static final int maxAges = 3;      //最多经历ags次GC，大的次数会导致旧数据被反复复制，错误的信息不能及时被丢弃
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

	public synchronized NewBufferBlock dispatcher(int count) throws Exception {
		if (isGC(count)) {
			int temp = 0;
			int beforced = 0;
			MessageBuffer survior = isF ? surviorFBf : primaryBf;  //isF为true时有效数据复制至surviorFBf
			BlockingQueue oldQue = bufferBlockQue;
			bufferBlockQue = new LinkedBlockingQueue();
			NewBufferBlock bufferBlock = (NewBufferBlock) oldQue.poll();
			while (bufferBlock != null) {
				BufferBlockProxy proxy = bufferBlock.proxy;
				if (proxy.bufferBlock.age > maxAges) {      //age仅在此同步块中被操作，无需同步
					proxy.clear();    //超过次数强制GC
					beforced += proxy.readCap();
					System.out.println(bufferBlock.toString() + "强制age:" + proxy.bufferBlock.age);
				}
				if (proxy.isAlive()) {
					try {
						temp += proxy.readCap();    //temp获取的可能是过期数据

						proxy.copyTo(survior);  //复制之后原引用地址指向新的byte[],newBB.alive=false,因为有对象指向原引用地址
					} catch (Exception e) {
						System.out.println("分配失败");
					}
				}
				bufferBlock = (NewBufferBlock) oldQue.poll();
			}
			if (isF) {
				primaryBf.reset();
			} else {
				surviorFBf.reset();
			}
			isF = !isF;
			System.out.println("GC成功:" + temp + "存活" + "  强制GC数量：" + beforced);
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
		return buffer.capacity.intValue() - buffer.writePos.intValue() < count;
	}

	static class Holder {
		static BufferRing bufferRing = new BufferRing();
	}
}
