package com.chat.socket;

import com.chat.cache.BufferBlock;
import com.chat.cache.BufferRing;
import com.chat.cache.util.RedisClientTool;
import com.chat.client.Client;
import com.chat.message.Message;
import com.chat.message.MessageAccept;
import com.chat.message.MessageSend;
import com.chat.message.NewWriteTask;
import com.chat.util.CodeUtil;
import com.chat.util.IdFactory;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:51
 * @UpdeteTime: 2020-06-17 10:51
 * @Description:
 */
public class WriterProcessor implements Runnable {
	static final String serverId;
	public static AtomicInteger integer;
	static byte[] serverIdBytes = new byte[0];

	static {
		byte[] bytes = new byte[IdFactory.IDLEN];
		for (int i = 0; i < IdFactory.IDLEN; i++)
			bytes[i] = '0';
		serverId = new String(bytes);
		integer = new AtomicInteger();
		try {
			serverIdBytes = CodeUtil.intToBinaryBytes(Message.len);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BlockingQueue outbound;    //热点域，使用LinkBlockingQue
	private ConcurrentMap mapOnLine;        //所有writer线程的热点域，使用concurrentHashMap
	private RedisClientTool tool = new RedisClientTool();
	static BufferRing bufferRing = BufferRing.getInstance();

	public WriterProcessor(BlockingQueue queue, ConcurrentMap onLine) {
		outbound = queue;
		mapOnLine = onLine;

	}

	@Override
	public void run() {
		while (true) {
			try {
				handlerMsg();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlerMsg() throws Exception {
		MessageAccept accept = (MessageAccept) outbound.poll();
		while (accept != null) {
			NewWriteTask task = (NewWriteTask) accept.tasks.poll(); //同一时间task只会被单个线程持有
			List notEndTask = new ArrayList();
			while (task != null) {
				//BufferBlock msg = task.msg;     //msg会被当前线程和内存回收线程同时操作
				if (isLogin(task.msg)) {
					String loginId = task.msg.toString(task.msg.getReadOff() + Message.len, IdFactory.IDLEN);
					task.msg.clear();
					mapOnLine.putIfAbsent(loginId, accept.sc);
					Message message = new Message();
					message.initMsg(loginId + "登陆成功", serverId);
					MessageSend send = new MessageSend(accept.sc);
					send.sendMsg(message);
					tool.setAdd(Client.redisKey, loginId);
				} else if (task.msg.isAlive()) {
					boolean sendEnd = false;
					BufferBlock id = task.ids.poll();
					while (id != null) {
						String accId = id.toString(id.getReadOff(), IdFactory.IDLEN);

						SocketChannel sc = (SocketChannel) mapOnLine.get(accId);
						if (sc != null && sc.isOpen()) {
							MessageSend send = new MessageSend(sc);
							BufferBlock outMsg = bufferRing.dispatcher(task.msg.readCap() + Message.len);
							copyMsg(task.msg, outMsg, task.msg.readCap());  //复制msg内容至outMsg
							outMsg.readFromBytes(serverIdBytes);    //为outMsg添加后缀，使其成为合法消息
							String data = outMsg.toString(outMsg.readOff, outMsg.readCap());
							int age = task.msg.age;
							send.sendMsg(outMsg);
							System.out.println("成功发送数量：" + integer.incrementAndGet() + "内容:  " + data + "age:" + age);
						}
						if (id.writeOut(id.getReadOff() + IdFactory.IDLEN) == '0') {     //已对有所ids完成msg发送
							sendEnd = true;
							task.msg.clear();
						}
						id.clear();
						id = task.ids.poll();
					}
					if (!sendEnd) {
						notEndTask.add(task);
					}
				} else if (!task.msg.isAlive()) {
					System.out.println(task.msg.toString() + " age:" + task.msg.age);
				}
				task = (NewWriteTask) accept.tasks.poll();
			}
			for (Object writeTask : notEndTask)
				accept.tasks.offer(writeTask);
			notEndTask.clear();
			accept = (MessageAccept) outbound.poll();
		}
	}

	private boolean isLogin(BufferBlock bufferBlock) {
		byte[] login = "login".getBytes();
		int i = -1;
		try {
			i = CodeUtil.findBytesByBM(bufferBlock.bytes, bufferBlock.readOff, bufferBlock.readCap(),
					login, 0, login.length);
		} catch (Exception e) {
			System.out.println("CodeUtil findBytesByBM error");
			e.printStackTrace();
		}
		return i != -1;
	}

	/**
	 * @param source    被复制的
	 * @param target    目标
	 * @param count
	 * @throws Exception 复制source的内容，不移动source的readOff指针
	 */
	private void copyMsg(BufferBlock source, BufferBlock target, int count) throws Exception {
		source.writeToOtherWithOutChange(target, count);
	}
}
