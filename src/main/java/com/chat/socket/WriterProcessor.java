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
	static final byte[] serverIdBytes;
	static {
		byte[] bytes = new byte[IdFactory.IDLEN];
		for (int i = 0; i < IdFactory.IDLEN; i++)
			bytes[i] = '0';
		serverId = new String(bytes);
		integer = new AtomicInteger();
		serverIdBytes = Integer.toBinaryString(10).getBytes();
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
			NewWriteTask task = (NewWriteTask) accept.tasks.poll();
			List notEndTask = new ArrayList();
			while (task != null) {
				BufferBlock msg = task.msg;
				if (isLogin(msg)) {
					String loginId = msg.toString(msg.readOff + Message.len, IdFactory.IDLEN);
					msg.clear();
					mapOnLine.putIfAbsent(loginId, accept.sc);
					Message message = new Message();
					message.initMsg(loginId + "登陆成功", serverId);
					MessageSend send = new MessageSend(accept.sc);
					send.sendMsg(message);
					tool.setAdd(Client.redisKey, loginId);
				} else {
					boolean sendEnd = false;
					BufferBlock id = task.ids.poll();
					while (id != null) {
						String accId = id.toString(id.readOff, IdFactory.IDLEN);

						SocketChannel sc = (SocketChannel) mapOnLine.get(accId);
						if (sc != null && sc.isOpen()) {
							MessageSend send = new MessageSend(sc);
							BufferBlock outMsg = bufferRing.dispatcher(msg.readCap() + Message.len);
							outMsg.readFromOther(msg, msg.readCap());
							msg.clear();
							outMsg.readFromBytes(serverIdBytes);
							send.sendMsg(outMsg);
							System.out.println("成功发送数量：" + integer.incrementAndGet() + "内容" + outMsg.toString(outMsg.readOff, outMsg.readCap()));
						}
						if (id.writeOut(id.readOff + IdFactory.IDLEN) == '0') {
							sendEnd = true;
						}
						if (!sendEnd) {
							notEndTask.add(task);
						}
						id.clear();
						id = task.ids.poll();
					}
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

}
