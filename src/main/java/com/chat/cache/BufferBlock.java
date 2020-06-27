package com.chat.cache;

/**
 * @Author: czw
 * @CreateTime: 2020-06-27 09:17
 * @UpdeteTime: 2020-06-27 09:17
 * @Description:
 */
public class BufferBlock {
	public byte[] bytes;
	public int offset;      //写指针位置
	public int count;       //剩余可写空间
	public int readOff;     //读指针位置
	public boolean alive;   //是否继续存活

	public BufferBlock(byte[] bytes, int offset, int count) {
		this.bytes = bytes;
		this.offset = offset;
		this.count = count;
		this.alive = true;
		this.readOff = offset;
		BufferRing.bufferBlockQue.offer(this);  //将所有bufferBlock实例传入que,this虽然没有构建完成但没影响
	}

	public void copyTo(BufferBlock newBB) throws Exception {    //仅供回收时使用
		if (count != newBB.count)
			throw new Exception("复制失败");
		System.arraycopy(bytes, readOff, newBB.bytes, newBB.offset, offset - readOff);
		newBB.offset += offset - readOff;
		newBB.alive = this.alive;   //copy后内存被回收,this指针无需处理
	}

	public void writeToOther(BufferBlock other, int count) throws Exception {
		if (count > offset - readOff || other.count < count)
			throw new Exception("bufferBlock读取错误");
		System.arraycopy(bytes, readOff, other.bytes, other.offset, count);
		readOff += count;
		other.offset += count;
		other.count -= count;

	}

	public void readFromOther(BufferBlock other, int count) throws Exception {
		if (this.count < count || other.offset - other.readOff < count)
			throw new Exception("bufferBlock写入错误");
		System.arraycopy(other.bytes, other.readOff, this.bytes, this.offset, count);
		this.offset += count;
		this.count -= count;
		other.readOff += count;
	}

	public void readFromBytes(byte[] bytes) throws Exception {
		if (this.count < bytes.length)
			throw new Exception("bufferBlock内存不足");
		System.arraycopy(bytes, 0, this.bytes, this.offset, bytes.length);
		this.offset += bytes.length;
		this.count -= bytes.length;
	}

	public void readFromBytes(byte[] bytes, int move) throws Exception {
		if (this.count < bytes.length + move)
			throw new Exception("bufferBlock内存不足");
		this.offset += move;
		this.count -= move;
		readFromBytes(bytes);
	}

	public void read(byte b) throws Exception {
		if (count < 1)
			throw new Exception("bufferBlock已满");
		bytes[offset++] = b;
		count--;
	}

	/**
	 * @return 剩余可读
	 */
	public int readCap() {
		return offset - readOff;
	}
}
