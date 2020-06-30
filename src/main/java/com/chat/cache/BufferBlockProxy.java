package com.chat.cache;

import com.chat.util.CodeUtil;

import java.nio.ByteBuffer;

/**
 * @Author: czw
 * @CreateTime: 2020-06-30 10:21
 * @UpdeteTime: 2020-06-30 10:21
 * @Description: 尝试为bufferBlock提供线程安全的方法
 */
public class BufferBlockProxy {
	private static final Object lock = new Object();
	public final NewBufferBlock bufferBlock;
	private final int local;

	public BufferBlockProxy(NewBufferBlock bufferBlock) {
		this.bufferBlock = bufferBlock;
		this.local = System.identityHashCode(bufferBlock);
		BufferRing.bufferBlockQue.offer(bufferBlock);
	}

	public void writeToOther(BufferBlockProxy proxy, int count) {
		Runnable runnable = () -> {
			try {
				writeToCheck(count);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			System.arraycopy(bufferBlock.bytes, bufferBlock.readOff,
					proxy.bufferBlock.bytes, proxy.bufferBlock.offset, count);
			bufferBlock.readOff += count;
			proxy.bufferBlock.offset += count;
			proxy.bufferBlock.count -= count;
		};
		operate(proxy, runnable);
	}

	private void operate(BufferBlockProxy proxy, Runnable runnable) {
		int i = complete(proxy);
		if (i > 0) {
			synchronized (bufferBlock) {
				synchronized (proxy.bufferBlock) {
					runnable.run();
				}
			}
		} else if (i < 0) {
			synchronized (proxy.bufferBlock) {
				synchronized (bufferBlock) {
					runnable.run();
				}
			}
		} else {
			synchronized (lock) {
				synchronized (bufferBlock) {
					synchronized (bufferBlock) {
						runnable.run();
					}
				}
			}
		}
	}

	public void readFromBytes(byte[] target, int offset, int count) {
		synchronized (bufferBlock) {
			try {
				readFromCheck(count);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			System.arraycopy(target, offset,
					bufferBlock.bytes, bufferBlock.offset, count);
			bufferBlock.offset += count;
			bufferBlock.count -= count;
		}
	}

	/**
	 * 将byteBuffer的数据写入bufferBlock
	 *
	 * @param byteBuffer
	 */
	public void readFromByteBuffer(ByteBuffer byteBuffer) {
		int num = byteBuffer.remaining();
		synchronized (bufferBlock) {
			try {
				readFromCheck(num);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			byteBuffer.get(bufferBlock.bytes, bufferBlock.offset, num);
			bufferBlock.offset += num;
			bufferBlock.count -= num;
		}
	}

	/**
	 * 将bufferBlock未读数据全部写入byteBuffer
	 *
	 * @param byteBuffer
	 */
	public void writeBackByteBuffer(ByteBuffer byteBuffer) {
		synchronized (bufferBlock) {
			int readCap = bufferBlock.offset - bufferBlock.readOff;
			if (readCap > 0) {
				byteBuffer.put(bufferBlock.bytes, bufferBlock.readOff, readCap);
			}
			bufferBlock.alive = false;
		}
	}

	/**
	 * 将bufferBlock未读数据写入byteBuffer
	 *
	 * @param byteBuffer
	 * @param count
	 */
	public void writeToBuffer(ByteBuffer byteBuffer, int count) {
		synchronized (bufferBlock) {
			try {
				writeToCheck(count);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			byteBuffer.put(bufferBlock.bytes, bufferBlock.readOff, count);
			bufferBlock.readOff += count;
		}
	}

	public int parseMsgLen(int msgLen) {
		synchronized (bufferBlock) {
			int mesLen = 0;
			try {
				mesLen = CodeUtil.bytesToInt(bufferBlock.bytes, bufferBlock.readOff, msgLen);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return mesLen;
		}
	}

	public void read(byte b) {
		synchronized (bufferBlock) {
			try {
				readFromCheck(1);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			bufferBlock.bytes[bufferBlock.offset++] = b;
			bufferBlock.count--;
		}
	}

	/**
	 * @param count 从readOff开始第count个元素
	 */
	public byte writeOut(int count) {
		synchronized (bufferBlock) {
			try {
				writeToCheck(count);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			return bufferBlock.bytes[bufferBlock.readOff + count - 1];
		}
	}

	/**
	 * @param count readOff偏移量
	 * @return
	 */
	public String toString(int count) {
		synchronized (bufferBlock) {
			try {
				writeToCheck(count);
			} catch (BufferBlockProxyExe bufferBlockProxyExe) {
				bufferBlockProxyExe.printStackTrace();
			}
			return new String(bufferBlock.bytes, bufferBlock.readOff, count);
		}
	}

	public boolean findBytes(byte[] bytes, int offset, int count) {
		synchronized (bufferBlock) {
			int i = -1;
			try {
				i = CodeUtil.findBytesByBM(bufferBlock.bytes, bufferBlock.readOff, readCap(),
						bytes, offset, count);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return i != -1;
		}
	}

	public void clear() {
		synchronized (bufferBlock) {
			bufferBlock.alive = false;
		}
	}

	public boolean isAlive() {
		synchronized (bufferBlock) {
			return bufferBlock.alive;
		}
	}

	public void copyTo(MessageBuffer messageBuffer) {
		synchronized (bufferBlock) {
			NewBufferBlock temp = null;
			try {
				temp = messageBuffer.dispatcher(readCap());
			} catch (Exception e) {
				e.printStackTrace();
			}

			int count = readCap();
			assert temp != null;
			System.arraycopy(bufferBlock.bytes, bufferBlock.readOff,
					temp.bytes, temp.offset, count);
			temp.offset += count;
			temp.count -= count;

			bufferBlock.bytes = temp.bytes;
			bufferBlock.offset = temp.offset;
			bufferBlock.count = temp.count;
			bufferBlock.readOff = temp.readOff;
			bufferBlock.age++;

			BufferRing.bufferBlockQue.offer(bufferBlock);       //重新入列
			temp.alive = false;
		}
	}


	public int readCap() {
		synchronized (bufferBlock) {
			return bufferBlock.offset - bufferBlock.readOff;
		}
	}

	public void offsetRightShift(int count) {
		synchronized (bufferBlock) {
			bufferBlock.offset += count;
			bufferBlock.count -= count;
		}
	}

	public void readOffRightShift(int count) {
		synchronized (bufferBlock) {
			bufferBlock.readOff += count;
		}
	}

	public void readOffLeftShift(int count) {
		synchronized (bufferBlock) {
			bufferBlock.readOff -= count;
		}
	}

	public int getCount() {
		synchronized (bufferBlock) {
			return bufferBlock.count;
		}
	}

	public int getAge() {
		synchronized (bufferBlock) {
			return bufferBlock.age;
		}
	}

	private int complete(BufferBlockProxy proxy) {
		return Integer.compare(local, proxy.local);
	}

	private void readFromCheck(int nums) throws BufferBlockProxyExe {
		if (bufferBlock.count < nums)
			throw new BufferBlockProxyExe("剩余写入空间不足");
	}

	private void writeToCheck(int nums) throws BufferBlockProxyExe {
		if (readCap() < nums)
			throw new BufferBlockProxyExe("剩余可读数据不足");
	}
}
