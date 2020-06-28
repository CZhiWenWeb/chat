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
	public boolean alive;   //是否继续存活,标记为false后随时可能被覆盖，不能继续使用
	private int readMark;   //标记当前readOff所在位置
	public int age;        //经历GC次数

	public BufferBlock(byte[] bytes, int offset, int count) {

		this.bytes = bytes;
		this.offset = offset;
		this.count = count;
		this.alive = true;
		this.readOff = offset;
		this.age = 0;
		BufferRing.bufferBlockQue.offer(this);  //将所有bufferBlock实例传入que,this虽然没有构建完成但没影响
	}

	public void copyTo(BufferBlock newBB) throws Exception {    //仅供回收时使用
		if (readCap() != newBB.count)
			throw new Exception("复制失败");
		System.arraycopy(bytes, readOff, newBB.bytes, newBB.offset, readCap());
		this.age++;     //每次存活年龄++
		newBB.offset += offset - readOff;
		newBB.alive = this.alive;
		newBB.age = this.age;
		this.clear();
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

	public byte writeOut(int index) throws Exception {
		if (index >= offset || index < readOff)
			throw new Exception("bufferBlock writeOut error");
		return bytes[index];
	}

	/**
	 * @return 剩余可读
	 */
	public int readCap() {
		return offset - readOff;
	}

	public String toString(int offset, int count) throws Exception {
		if (offset < readOff || count > this.offset - offset)
			throw new Exception("bufferBlock toString error");
		return new String(bytes, offset, count);
	}

	public void markReadOff() {
		this.readMark = this.readOff;
	}

	public void rollBack() {
		this.readOff = this.readMark;
	}

	public void clear() {
		this.alive = false;
	}
}
