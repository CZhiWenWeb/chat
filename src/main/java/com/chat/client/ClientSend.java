package com.chat.client;

import com.chat.cache.util.RedisClientTool;
import com.chat.message.Message;
import com.chat.message.MessageSend;
import com.chat.util.IdFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:19
 * @UpdeteTime: 2020-06-17 11:19
 * @Description:
 */
public class ClientSend implements Runnable {
	public SocketChannel sc;
	private SocketAddress address;
	private long id;
	private MessageSend msgSend;
	private RedisClientTool redisClientTool = new RedisClientTool();
	public static int maxToNums = 50;
	public ClientSend(int port) throws IOException {
		this.sc = SocketChannel.open();
		this.address = new InetSocketAddress("127.0.0.1", port);
		sc.configureBlocking(false);
		this.id = IdFactory.createId();
		this.msgSend = new MessageSend(sc);
	}

	@Override
	public void run() {
		try {
			sc.connect(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			while (!sc.finishConnect()) {
				Thread.sleep(100);
			}

			login();

			Thread.sleep(1000);

			sendToMany();

			//while (true) {
			//	Thread.sleep(new Random().nextInt(10) * 1000 +
			//			1000);
			//}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendToOne() throws Exception {
		//Set set = redisClientTool.getAllValues(Client.redisKey);
		//String msg = "好好学习tiantianxiangshang";
		//byteBuffer.put(
		//		createMsg(msg, String.valueOf(set.iterator().next()))
		//				.getMsg());
		//byteBuffer.flip();
		//sc.write(byteBuffer);
		//byteBuffer.clear();
	}

	private void sendToMany() throws Exception {
		String msg = "好好学习tiantianxiangshang";

		Set set = redisClientTool.getAllValues(Client.redisKey);
		Iterator it = set.iterator();
		int nums = set.size() > maxToNums ? maxToNums : set.size();
		String[] strings = new String[nums];  //发送的ids
		for (int i = 0; i < nums; i++)
			strings[i] = (String) it.next();
		Message message = new Message();
		message.initMsg(msg, String.valueOf(id), strings);
		msgSend.sendMsg(message);
	}

	private void login() throws Exception {
		String send = String.valueOf(id);
		String body = "login";
		Message msg = new Message();
		msg.initMsg(body, send);
		msgSend.sendMsg(msg);
	}

	public static void main(String[] args) throws IOException {
		ClientSend msgSend = new ClientSend(12346);
		new Thread(msgSend).start();
	}
}
