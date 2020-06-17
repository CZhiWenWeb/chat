package com.chat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

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

			while (true) {
				send();
				Thread.sleep(new Random().nextInt(10) * 1000 +
						1000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void send() throws IOException {
		byteBuffer.put((id + ":hello").getBytes());
		byteBuffer.flip();
		sc.write(byteBuffer);
		byteBuffer.clear();
	}

	public static void main(String[] args) throws IOException {
		MsgSend msgSend = new MsgSend(12346);
		new Thread(msgSend).start();
	}
}
