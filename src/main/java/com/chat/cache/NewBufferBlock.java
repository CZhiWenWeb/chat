package com.chat.cache;

/**
 * @Author: czw
 * @CreateTime: 2020-06-30 10:18
 * @UpdeteTime: 2020-06-30 10:18
 * @Description:这个类的所有属性都是热点域，所以任何使用和回收应该是互斥的原子操作
 */
public class NewBufferBlock {
	public byte[] bytes;
	public int offset;
	public int count;
	public int readOff;
	public boolean alive;
	public int age;
	public BufferBlockProxy proxy;

	public NewBufferBlock(byte[] bytes, int offset, int count) {
		this.bytes = bytes;
		this.offset = offset;
		this.count = count;
		this.alive = true;
		this.readOff = offset;
		this.age = 0;
		this.proxy = new BufferBlockProxy(this);
	}
}
