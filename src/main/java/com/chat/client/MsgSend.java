package com.chat.client;

import com.chat.cache.util.RedisClientTool;
import com.chat.util.IdFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:19
 * @UpdeteTime: 2020-06-17 11:19
 * @Description:
 */
public class MsgSend implements Runnable {
	public SocketChannel sc;
	private SocketAddress address;
	private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
	private long id;
	private RedisClientTool redisClientTool = new RedisClientTool();
	public static int maxToNums = 5;
	public MsgSend(int port) throws IOException {
		this.sc = SocketChannel.open();
		this.address = new InetSocketAddress("127.0.0.1", port);
		sc.configureBlocking(false);
		this.id = IdFactory.createId();
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
		Set set = redisClientTool.getAllValues(Client.redisKey);
		String msg = "好好学习tiantianxiangshang";
		byteBuffer.put(
				createMsg(msg, String.valueOf(set.iterator().next()))
						.getMsg());
		byteBuffer.flip();
		sc.write(byteBuffer);
		byteBuffer.clear();
	}

	private void sendToMany() throws Exception {
		String msg = "好好学习tiantianxiangshang";

		Set set = redisClientTool.getAllValues(Client.redisKey);
		String[] strings = new String[maxToNums];  //发送的ids
		Iterator it = set.iterator();
		int nums = set.size() > maxToNums ? maxToNums : set.size();
		for (int i = 0; i < nums; i++)
			strings[i] = (String) it.next();
		Message message = createMsg(msg, strings);

		int len = message.len();
		int start = 0;
		int i = write(message.getMsg(), start, len);
		while (i < len) {
			len -= i;
			start += i;
			i = write(message.getMsg(), start, len);
		}

		System.out.println("megLen:" + message.len() + " write:" + i);
	}

	private int write(byte[] bytes, int start, int len) throws IOException {
		byteBuffer.put(bytes, start, len);
		byteBuffer.flip();
		int i = sc.write(byteBuffer);
		byteBuffer.clear();
		return i;
	}

	private Message createMsg(String msg, String... to) throws Exception {
		if (to.length < 1)
			throw new Exception("至少一个收件人");
		StringBuilder temp = new StringBuilder();
		for (int i = 1; i < to.length - 1; i++) {   //去头去尾
			temp.append(to[i]).append('1');
		}
		if (to.length > 1)
			temp.append(to[to.length - 1]).append('0');
		temp.append(msg);
		StringBuilder header = new StringBuilder(
				Integer.toBinaryString(temp.toString().getBytes().length + Message.startIndex)   //计算消息长度
						+ id + "to" + to[0]);
		if (to.length > 1) {
			header.append('1');
		} else {
			header.append('0');
		}
		Message message = new Message();
		message.initMsg(temp.toString(), header.toString());
		return message;
	}

	private void login() throws IOException {

		byteBuffer.put((id + ":login").getBytes());
		byteBuffer.flip();
		sc.write(byteBuffer);
		byteBuffer.clear();
	}

	public static void main(String[] args) throws IOException {
		MsgSend msgSend = new MsgSend(12346);
		new Thread(msgSend).start();
	}
}
