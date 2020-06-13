package com.chat.cache.message;

import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-13 09:03
 * @UpdeteTime: 2020-06-13 09:03
 * @Description:
 */
public class AllocateBufferTest {

	@Test
	public void test() {
		//新建实例
		AllocateBuffer bufferPool = new AllocateBuffer();

		//分发
		BufferBlock block1 = bufferPool.take();

		assert block1.capacity == 1024;
		assert block1.writePos == 0;
		assert block1.mark == 0;
		assert block1.buffer == bufferPool.smallMessageBuffer;

		//扩容
		block1 = block1.bufferPool.expand(block1);
		assert block1.capacity == 128 * 1024;

		//回收
		block1 = (BufferBlock) block1.bufferPool.put(block1);
		assert block1 == null;
		bufferPool.take();
	}

	@Test
	public void messageTest() {
		byte[] bytes = "hello world".getBytes();
		AllocateBuffer allocateBuffer = new AllocateBuffer();
		Message message = new Message(allocateBuffer.take());
		message.writeToMessage(bytes, 0, bytes.length - 1);
		Message message1 = new Message(allocateBuffer.take());
		message1.writeToMessage(bytes, 0, bytes.length - 1);
		assert allocateBuffer.smallMessageBuffer[0] == allocateBuffer.smallMessageBuffer[1024];
	}

	@Test
	public void ss(){
		String s="aa\r\nbbb";
	}
}
