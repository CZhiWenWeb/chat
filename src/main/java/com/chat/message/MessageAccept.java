package com.chat.message;

import com.chat.cache.BufferBlock;
import com.chat.cache.BufferRing;
import com.chat.util.CodeUtil;
import com.chat.util.IdFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-23 14:14
 * @UpdeteTime: 2020-06-23 14:14
 * @Description:
 */
public class MessageAccept {

	public SocketChannel sc;
	private ByteBuffer readBuffer = ByteBuffer.allocate(MessageSend.cap);

	private BufferBlock completeMsg;    //header+body
	private Queue queue;                //accIds
	public BlockingQueue tasks = new LinkedBlockingQueue();       //热点域，使用线程安全的que
	static BufferRing bufferRing = BufferRing.getInstance();
	private int accLen = -1;
	public MessageAccept(SocketChannel sc) {
		this.sc = sc;
	}

	public int read() throws Exception {
		int i = sc.read(readBuffer);
		if (i > 0) {
			readBuffer.flip();
			System.out.println(i);

			BufferBlock bufferBlock = bufferRing.dispatcher(readBuffer.remaining());

			readBuffer.get(bufferBlock.bytes, bufferBlock.offset, bufferBlock.count);
			bufferBlock.offset += bufferBlock.count;
			bufferBlock.count = 0;
			readBuffer.clear();

			handlerBytes(bufferBlock);
		}
		return i;
	}

	private void handlerBytes(BufferBlock bufferBlock) throws Exception {
		if (bufferBlock.readCap() <= 0) {
			bufferBlock.clear();
			return;
		}
		if (completeMsg == null) {      //生成新消息
			if (bufferBlock.readCap() < Message.len) {      //不足解析消息长度，回写，数据用尽，并标记bb可回收
				writeBlack(bufferBlock);
				return;
			} else {
				int msgLen = CodeUtil.bytesToInt(bufferBlock.bytes, bufferBlock.readOff, Message.len);
				completeMsg = bufferRing.dispatcher(msgLen);    //为完整消息一次性分配内存
			}
		}
		if (bufferBlock.readCap() > completeMsg.count) {    //接受到完整的header和body
			if (queue == null) {
				completeMsg.readFromOther(bufferBlock, completeMsg.count);  //写入completeMsg
				queue = new LinkedList();
				NewWriteTask task = new NewWriteTask(completeMsg, queue);
				tasks.offer(task);
				parseAcceptIds(bufferBlock);    //解析ids
			} else {
				parseAcceptIds(bufferBlock);
			}
		} else {
			completeMsg.readFromOther(bufferBlock, bufferBlock.readCap());
			bufferBlock.clear();     //bufferBlock读尽，标记可回收
		}
	}

	private void parseAcceptIds(BufferBlock bufferBlock) throws Exception {
		if (accLen == -1 && bufferBlock.readCap() < Message.len) {
			writeBlack(bufferBlock);
		} else {
			if (accLen == -1) {  //解析accId长度
				accLen = CodeUtil.bytesToInt(bufferBlock.bytes, bufferBlock.readOff, Message.len);
				bufferBlock.readOff += Message.len;
				accLen -= Message.len;
				parseAcceptIds(bufferBlock);
			} else {
				if (accLen != 0) {
					if (bufferBlock.readCap() >= IdFactory.IDLEN) {
						BufferBlock taskBf = bufferRing.dispatcher(IdFactory.IDLEN + 1);
						bufferBlock.writeToOther(taskBf, IdFactory.IDLEN);
						queue.offer(taskBf);    //accId入列
						accLen -= IdFactory.IDLEN;
						if (accLen == 0) {
							taskBf.read((byte) '0');    //0标识ids接收完毕
							reset();
							handlerBytes(bufferBlock);  //粘包处理
						} else {
							taskBf.read((byte) '1');
							parseAcceptIds(bufferBlock);
						}
					} else {    //长度不足获取id
						writeBlack(bufferBlock);
					}
				} else {    //ids解析完毕，继续解析下一条消息
					reset();
					handlerBytes(bufferBlock);
				}
			}
		}
	}

	private void reset() {
		completeMsg = null;
		accLen = -1;
		queue = null;
	}

	private void writeBlack(BufferBlock bufferBlock) {
		if (bufferBlock.readCap() <= 0) {
			bufferBlock.clear();
			return;
		}
		readBuffer.put(bufferBlock.bytes, bufferBlock.readOff, bufferBlock.readCap());
		bufferBlock.clear();
	}
}
