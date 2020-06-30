package com.chat.socket;

import com.chat.cache.BufferBlockProxy;
import com.chat.cache.BufferRing;
import com.chat.cache.NewBufferBlock;
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
				if (isLogin(task.msg)) {
					task.msg.readOffRightShift(Message.len);    //忽略长度解析
					String loginId = task.msg.toString(IdFactory.IDLEN);
					task.msg.clear();
					mapOnLine.putIfAbsent(loginId, accept.sc);
					Message message = new Message();
					message.initMsg(loginId + "登陆成功", serverId);
					MessageSend send = new MessageSend(accept.sc);
					send.sendMsg(message);
					tool.setAdd(Client.redisKey, loginId);
				} else if (task.msg.isAlive()) {
					boolean sendEnd = false;
					BufferBlockProxy id = task.ids.poll();
					while (id != null) {
						String accId = id.toString(IdFactory.IDLEN);
						SocketChannel sc = (SocketChannel) mapOnLine.get(accId);
						if (sc != null && sc.isOpen()) {
							MessageSend send = new MessageSend(sc);
							NewBufferBlock bufferBlock = bufferRing.dispatcher(task.msg.readCap() + Message.len);
							BufferBlockProxy outMsg = bufferBlock.proxy;
							int readCap = task.msg.readCap();
							task.msg.writeToOther(outMsg, readCap); //复制msg内容至outMsg
							task.msg.readOffLeftShift(readCap);
							outMsg.readFromBytes(serverIdBytes, 0, serverIdBytes.length);    //为outMsg添加后缀，使其成为合法消息
							String data = outMsg.toString(outMsg.readCap());
							int age = task.msg.getAge();
							send.sendMsg(outMsg);
							System.out.println("成功发送数量：" + integer.incrementAndGet() + "内容:  " + data + "age:" + age);
						}

						if (id.writeOut(IdFactory.IDLEN + 1) == '0') {     //已对有所ids完成msg发送
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
					System.out.println(task.msg.toString() + " age:" + task.msg.getAge());
				}
				task = (NewWriteTask) accept.tasks.poll();
			}
			for (Object writeTask : notEndTask)
				accept.tasks.offer(writeTask);
			notEndTask.clear();
			accept = (MessageAccept) outbound.poll();
		}
	}

	private boolean isLogin(BufferBlockProxy proxy) {
		byte[] login = "login".getBytes();
		return proxy.findBytes(login, 0, login.length);
	}
}
