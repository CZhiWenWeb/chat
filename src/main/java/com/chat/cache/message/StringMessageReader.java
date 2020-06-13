package com.chat.cache.message;

import com.chat.socket.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: czw
 * @CreateTime: 2020-06-13 15:10
 * @UpdeteTime: 2020-06-13 15:10
 * @Description: 定义String类型消息格式
 * mark+            mark为client随机生成的4位数，防止读取错误边界
 * {body}+\r\n
 * mark+\r\n
 */
public class StringMessageReader implements MessageReader {
	private AllocateBuffer buffer;

	private Message nextMsg;

	private List<Message> completeMessage = new ArrayList<>();

	@Override
	public void init(AllocateBuffer allocateBuffer) {
		buffer = allocateBuffer;
		nextMsg = new Message(buffer.take());
	}

	@Override
	public void read(Socket socket, ByteBuffer byteBuffer) throws IOException {
		socket.read(byteBuffer);
		byteBuffer.flip();

		if (byteBuffer.remaining() == 0) {
			byteBuffer.clear();
			return;
		}

		nextMsg.writeToMessage(byteBuffer);

		int endIndex = parseString(nextMsg);


	}

	@Override
	public List<Message> getMessage() {
		return this.completeMessage;
	}

	private int parseString(Message message) {
		BufferBlock block = message.messageBf;
		byte[] bytes = block.buffer;
		int offset = block.readPos;
		int len = block.writeLen();
		String msg = new String(bytes, offset, len);
		if (message.startMark == -1) {
			//消息头必然为起始4位，直接获取
			message.startMark = Integer.parseInt(msg.substring(0, 4));
		}


		int end = msg.indexOf(message.startMark + "\r\n");
		//粘包，进行拆分
		if (msg.length() - end > 6) {
			//分配一个新的内存
			BufferBlock newBlock = block.bufferPool.take();
			//截断
			block.writePos = block.readPos + end + 5;
			//添加完整信息
			completeMessage.add(message);


			newBlock.readPos = block.writePos + 1;

			nextMsg = new Message(newBlock);
		}

		return 0;

	}

}
