package com.chat.socket;

import com.chat.cache.util.RedisClientTool;
import com.chat.message.Message;
import com.chat.message.MessageAccept;
import com.chat.message.NewWriteTask;
import com.chat.util.CodeUtil;
import com.chat.util.IdFactory;

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

	static {
		byte[] bytes = new byte[IdFactory.IDLEN];
		for (int i = 0; i < IdFactory.IDLEN; i++)
			bytes[i] = '0';
		serverId = new String(bytes);
		integer = new AtomicInteger();
	}

	private BlockingQueue outbound;    //热点域，使用LinkBlockingQue
	private ConcurrentMap mapOnLine;        //所有writer线程的热点域，使用concurrentHashMap
	private RedisClientTool tool = new RedisClientTool();

	public WriterProcessor(BlockingQueue queue, ConcurrentMap onLine) {
		outbound = queue;
		mapOnLine = onLine;

	}

	@Override
	public void run() {
		while (true) {
			handlerMsg();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handlerMsg() {
		MessageAccept accept = (MessageAccept) outbound.poll();
		while (accept != null) {
			NewWriteTask task = (NewWriteTask) accept.tasks.poll();
			List notEndTask = new ArrayList();
			while (task != null) {

				task = (NewWriteTask) accept.tasks.poll();
			}
			accept = (MessageAccept) outbound.poll();
		}
	}
	//private void handlerMsg() {
	//	MessageAccept accept = (MessageAccept) outbound.poll();
	//	while (accept != null) {
	//		NewWriteTask task = (NewWriteTask) accept.tasks.poll();
	//		List notEndTask = new ArrayList();
	//		while (task != null) {
	//			byte[] msg = task.msg;
	//			if (isLogin(msg)) {
	//				String loginId = new String(msg, Message.len, IdFactory.IDLEN);
	//				mapOnLine.putIfAbsent(loginId, accept.sc);
	//				Message message = new Message();
	//				try {
	//					message.initMsg(loginId + " 登入成功", serverId);
	//					MessageSend send = new MessageSend(accept.sc);
	//					send.sendMsg(message);  //发送登入通知
	//					tool.setAdd(Client.redisKey, loginId);
	//				} catch (Exception e) {
	//					e.printStackTrace();
	//				}
	//			} else {
	//				boolean sendEnd = false;
	//				byte[] id = task.accIds.poll();
	//				while (id != null) {
	//					String accId = new String(id, 0, IdFactory.IDLEN);
	//					SocketChannel sc = (SocketChannel) mapOnLine.get(accId);
	//					if (sc != null && sc.isOpen()) {
	//						MessageSend send = new MessageSend(sc);
	//						try {
	//							Message message = new Message();
	//							message.initMsgWithOutAccIds(msg);
	//							send.sendMsg(message);      //发送消息
	//							integer.incrementAndGet();
	//							System.out.println("发送成功：" + integer.intValue() + "内容：" + new String(msg));
	//						} catch (IOException e) {
	//							System.out.println("MessageSend sendMsg error");
	//							e.printStackTrace();
	//						} catch (Exception e) {
	//							System.out.println("Message initMsgWithOutAccIds error");
	//							e.printStackTrace();
	//						}
	//					}
	//					if (id[IdFactory.IDLEN] == '0') {
	//						sendEnd = true;
	//					}
	//					id = task.accIds.poll();
	//				}
	//				if (!sendEnd) {
	//					notEndTask.add(task);//还有其他ids需要接收msg，重新放入队列，等待ids到来
	//				}
	//			}
	//			task = accept.tasks.poll();
	//		}
	//		for (Object writeTask : notEndTask) {      //重新插入
	//			accept.tasks.offer((WriteTask) writeTask);
	//		}
	//		notEndTask.clear();
	//		accept = (MessageAccept) outbound.poll();
	//	}
	//}

	private boolean isLogin(byte[] bytes) {
		byte[] login = "login".getBytes();
		int i = 0;
		try {
			i = CodeUtil.findBytesByBM(bytes, Message.startIndex, login.length,
					login, 0, login.length);
		} catch (Exception e) {
			System.out.println("CodeUtil findBytesByBM error");
			e.printStackTrace();
		}
		return i != -1;
	}
}
