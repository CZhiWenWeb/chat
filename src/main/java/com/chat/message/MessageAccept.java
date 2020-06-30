package com.chat.message;

import com.chat.cache.BufferBlockProxy;
import com.chat.cache.BufferRing;
import com.chat.cache.NewBufferBlock;
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

	private BufferBlockProxy completeMsg;    //header+body
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

			NewBufferBlock bufferBlock = bufferRing.dispatcher(readBuffer.remaining());
			BufferBlockProxy proxy = bufferBlock.proxy;

			proxy.readFromByteBuffer(readBuffer);   //写入数据
			readBuffer.clear();

			handlerBytes(proxy);
		}
		return i;
	}

	private void handlerBytes(BufferBlockProxy proxy) throws Exception {    //此处proxy只可能被回收线程和当前线程操作
		if (proxy.readCap() <= 0) {
			proxy.clear();
			return;
		}
		if (completeMsg == null) {      //生成新消息
			if (proxy.readCap() < Message.len) {
				proxy.writeBackByteBuffer(readBuffer);   //不足解析消息长度，回写，数据用尽，并标记bb可回收
				return;
			} else {
				int msgLen = proxy.parseMsgLen(Message.len);
				NewBufferBlock bufferBlock = bufferRing.dispatcher(msgLen); //为完整消息一次性分配内存
				completeMsg = bufferBlock.proxy;
			}
		}
		if (proxy.readCap() > completeMsg.getCount()) {    //接受到完整的header和body
			if (queue == null) {
				proxy.writeToOther(completeMsg, completeMsg.getCount());    //写入completeMsg
				queue = new LinkedList();
				NewWriteTask task = new NewWriteTask(completeMsg, queue);
				tasks.offer(task);
				parseAcceptIds(proxy);    //解析ids
			} else {
				parseAcceptIds(proxy);
			}
		} else {
			proxy.writeToOther(completeMsg, proxy.readCap());
			proxy.clear();     //bufferBlock读尽，标记可回收
		}
	}

	private void parseAcceptIds(BufferBlockProxy proxy) throws Exception {
		if (accLen == -1 && proxy.readCap() < Message.len) {
			proxy.writeBackByteBuffer(readBuffer);
		} else {
			if (accLen == -1) {  //解析accId长度
				accLen = proxy.parseMsgLen(Message.len);
				proxy.readOffRightShift(Message.len);
				accLen -= Message.len;
				parseAcceptIds(proxy);
			} else {
				if (accLen != 0) {
					if (proxy.readCap() >= IdFactory.IDLEN) {
						NewBufferBlock bufferBlock = bufferRing.dispatcher(IdFactory.IDLEN + 1);
						BufferBlockProxy idProxy = bufferBlock.proxy;
						proxy.writeToOther(idProxy, IdFactory.IDLEN);
						queue.offer(idProxy);    //accId入列
						accLen -= IdFactory.IDLEN;
						if (accLen == 0) {
							idProxy.read((byte) '0');    //0标识ids接收完毕
							reset();
							handlerBytes(proxy);  //粘包处理
						} else {
							idProxy.read((byte) '1');
							parseAcceptIds(proxy);
						}
					} else {    //长度不足获取id
						proxy.writeBackByteBuffer(readBuffer);
					}
				} else {    //ids解析完毕，继续解析下一条消息
					reset();
					handlerBytes(proxy);
				}
			}
		}
	}

	private void reset() {
		completeMsg = null;
		accLen = -1;
		queue = null;
	}
}
