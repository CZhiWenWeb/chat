package com.chat.cache.message;

import com.chat.exception.TakeBlockExe;

/**
 * @Author: czw
 * @CreateTime: 2020-06-12 15:56
 * @UpdeteTime: 2020-06-12 15:56
 * @Description:信息读写缓冲区，每个socketIO处理线程使用一个实例
 */
public class AllocateBuffer {
	public static int KB = 1024;
	public static int MB = 1024 * KB;

	private static final int CAPACITY_SAMLL = KB;
	private static final int CAPACITY_MEDIUM = 128 * KB;
	private static final int CAPACITY_LARGE = MB;

	//内存池
	private int smallBlockNum = 1024;
	private int mediuBlockNum = 68;
	byte[] smallMessageBuffer = new byte[smallBlockNum * CAPACITY_SAMLL];
	byte[] mediumMessageBuffer = new byte[mediuBlockNum * CAPACITY_MEDIUM];

	//分发内存块
	RingBufferFlip smallMessageBufferFreeBlock = new RingBufferFlip(smallBlockNum);
	RingBufferFlip mediumMessageBufferFreeBlock = new RingBufferFlip(mediuBlockNum);

	public AllocateBuffer() {
		//	将空闲的切面放入ringBufferFlip
		for (int i = 0; i < smallBlockNum; i++)
			this.smallMessageBufferFreeBlock.put(i);
		for (int i = 0; i < mediuBlockNum; i++)
			this.mediumMessageBufferFreeBlock.put(i);
	}

	//发放内存块
	public BufferBlock take() {
		Object start = smallMessageBufferFreeBlock.take();
		if (start != null) {
			return new BufferBlock(smallMessageBuffer, (int) start, CAPACITY_SAMLL, this);
		} else {
			throw new TakeBlockExe("take false");
		}
	}

	//回收内存块
	public Object put(BufferBlock block) {
		boolean result = false;
		if (block.bufferPool == this) {
			if (block.capacity == CAPACITY_SAMLL) {
				result = smallMessageBufferFreeBlock.put(block.mark);
			} else if (block.capacity == CAPACITY_MEDIUM) {
				result = mediumMessageBufferFreeBlock.put(block.mark);
			}
		}
		if (!result)
			throw new TakeBlockExe("内存回收失败");
		//让被回收block指向null,不再被使用
		return null;
	}

	//扩展内存块
	public BufferBlock expand(BufferBlock block) {
		if (block.capacity == CAPACITY_SAMLL) {
			Object mark = mediumMessageBufferFreeBlock.take();
			BufferBlock newBlock = new BufferBlock(mediumMessageBuffer, (int) mark, CAPACITY_MEDIUM, this);
			//存在旧数据，复制到新数组
			if (block.writePos > block.readPos)
				moveMess(smallMessageBuffer, mediumMessageBuffer, block,
						(int) mark * CAPACITY_MEDIUM);
			//移动newBlock的write指针
			newBlock.writePos += block.writeLen();
			//回收BufferBlock
			put(block);
			return newBlock;
		} else {
			throw new TakeBlockExe("expandBlock false");
		}
	}

	private void moveMess(byte[] oldBf, byte[] newBf, BufferBlock block, int newWritePos) {
		System.arraycopy(oldBf, block.readPos,
				newBf, newWritePos,
				block.writeLen());
	}

}
