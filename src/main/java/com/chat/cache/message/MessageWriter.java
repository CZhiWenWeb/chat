package com.chat.cache.message;

import com.chat.socket.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: czw
 * @CreateTime: 2020-06-12 17:04
 * @UpdeteTime: 2020-06-12 17:04
 * @Description:
 */
public class MessageWriter {
	//初始化队列，存放写入的信息
	private List<Message> writeQue = new LinkedList<>();

	//先进先出
	public void enqueue(Message message) {
		writeQue.add(message);
	}

	public void write(Socket socket, ByteBuffer byteBuffer) throws IOException {
		if (!writeQue.isEmpty()) {
			Message message = writeQue.remove(0);
			//将msg读入bf
			byteBuffer.put(message.messageBf.buffer,
					message.messageBf.readPos, message.messageBf.writeLen());
			byteBuffer.flip();

			//内存回收
			BufferBlock block = message.messageBf;
			block.bufferPool.put(block);

			socket.write(byteBuffer);
		}
	}

	public boolean isEmpty() {
		return this.writeQue.isEmpty();
	}
}
