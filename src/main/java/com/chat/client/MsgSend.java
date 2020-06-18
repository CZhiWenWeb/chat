package com.chat.client;

import com.chat.cache.util.RedisClientTool;
import com.chat.socket.ReaderProcessor;
import com.chat.socket.SocketReader;
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
	private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
	private long id;
	private RedisClientTool redisClientTool = new RedisClientTool();
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

			Thread.sleep(1000 * 10);    //等待在线检测

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
		SocketReader socketReader = ReaderProcessor.randomClientor();
		String msg = "好好学习tiantianxiangshang";
		byteBuffer.put(
				createMsg(msg, String.valueOf(socketReader.socketId))
						.getMsg());
		byteBuffer.flip();
		sc.write(byteBuffer);
		byteBuffer.clear();
	}

	private void sendToMany() throws Exception {
		String msg = "好好学习tiantianxiangshang";

		Set set = redisClientTool.getAllValues(Client.redisKey);
		String[] strings = new String[10];
		Iterator it = set.iterator();
		for (int i = 0; i < 10; i++)
			strings[i] = (String) it.next();
		byteBuffer.put(
				createMsg(msg, strings).getMsg()
		);
		byteBuffer.flip();
		sc.write(byteBuffer);
		byteBuffer.clear();
	}

	private Message createMsg(String msg, String... to) throws Exception {
		StringBuilder header = new StringBuilder(Integer.toBinaryString(msg.length() + Message.startIndex) + id + "to" + to[0]);
		if (to.length > 1) {
			header.append('1');
		} else {
			header.append('0');
		}
		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < to.length - 1; i++) {
			temp.append(to[i]).append('1');
		}
		temp.append(to[to.length - 1]).append('0');
		temp.append(msg);
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
