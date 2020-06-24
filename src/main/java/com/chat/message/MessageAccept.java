package com.chat.message;

import com.chat.util.CodeUtil;
import com.chat.util.IdFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-23 14:14
 * @UpdeteTime: 2020-06-23 14:14
 * @Description:
 */
public class MessageAccept {

	public SocketChannel sc;
	private ByteBuffer readBuffer = ByteBuffer.allocate(MessageSend.cap);
	private int msgLen = -1;    //剩余消息长度
	private int accLen = -1;
	private int writeIndex = 0; //
	private byte[] completeMsg;
	private Queue queue;
	private WriteTask task;
	public Queue<WriteTask> tasks = new LinkedList<>();

	public MessageAccept(SocketChannel sc) {
		this.sc = sc;
	}

	public int read() throws Exception {
		int i = sc.read(readBuffer);
		readBuffer.flip();
		if (i > 0) {
			System.out.println(i);
			byte[] bytes = new byte[readBuffer.remaining()];
			readBuffer.get(bytes);
			readBuffer.clear();

			handlerBytes(bytes, 0, bytes.length);
		}
		return i;
	}

	private void handlerBytes(byte[] bytes, int offset, int count) throws Exception {
		if (count == 0)
			return;
		if (completeMsg == null) {  //新的消息
			//不完整的消息长度信息，回写入bf
			if (count < Message.len) {
				readBuffer.put(bytes, offset, count);
				return;
			} else {
				msgLen = CodeUtil.bytesToInt(bytes, 0, Message.len);
				completeMsg = new byte[msgLen];     //分配消息内存
			}
		}
		if (count > msgLen) {      //head与body接收完毕
			if (queue == null) {
				System.arraycopy(bytes, offset, completeMsg, writeIndex, msgLen);  //写入消息
				queue = new LinkedList();
				task = new WriteTask(completeMsg, queue);
				tasks.offer(task);      //开始接收ids即放入队列
				writeIndex = 0;
				parseAcceptIds(bytes, offset + msgLen, count - msgLen);  //接收accIds
			} else {
				parseAcceptIds(bytes, offset, count);
			}
		} else {
			System.arraycopy(bytes, offset, completeMsg, writeIndex, count);  //写入消息
			writeIndex += count; //移动写指针
			msgLen -= count;  //修改剩余消息量
		}
	}

	private void parseAcceptIds(byte[] bytes, int offset, int len) throws Exception {
		if (accLen == -1 && len < Message.len) {
			readBuffer.put(bytes, offset, len);
		} else {
			if (accLen == -1) {      //解析accIds长度
				accLen = CodeUtil.bytesToInt(bytes, offset, Message.len);
				writeIndex += Message.len;
				offset += Message.len;
				len -= Message.len;
				parseAcceptIds(bytes, offset, len);
			} else {
				if (accLen != writeIndex) {
					if (len >= IdFactory.IDLEN) {
						byte[] accId = new byte[IdFactory.IDLEN + 1];   //最后一位用来标记结束
						System.arraycopy(bytes, offset, accId, 0, IdFactory.IDLEN);
						offset += IdFactory.IDLEN;
						len -= IdFactory.IDLEN;
						writeIndex += IdFactory.IDLEN;
						queue.offer(accId);     //接收id入列
						if (accLen == writeIndex) {
							accId[IdFactory.IDLEN] = '0';
							reset();   //消息与接收id接收完毕
							handlerBytes(bytes, offset, len);   //粘包处理
						} else {
							accId[IdFactory.IDLEN] = '1';
							parseAcceptIds(bytes, offset, len);
						}
					} else {
						readBuffer.put(bytes, offset, len);
					}
				} else {
					reset();
					handlerBytes(bytes, offset, len);
				}
			}
		}
	}

	private void reset() {
		completeMsg = null;
		writeIndex = 0;
		msgLen = -1;
		queue = null;
		accLen = -1;
		task = null;
	}

}
