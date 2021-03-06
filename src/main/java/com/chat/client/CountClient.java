package com.chat.client;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 16:51
 * @UpdeteTime: 2020-06-17 16:51
 * @Description:并发测试 群发消息数量应为 clientNum*maxToNum
 * 总消息数量为   clientNum*maxToNum  +  clientNum
 */
public class CountClient extends Client {
	static int NUMs = 1500;
	static CountDownLatch count = new CountDownLatch(NUMs);

	public CountClient(int port) throws IOException {
		super(port);
	}

	@Override
	public void run() {
		try {
			count.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		super.run();
	}

	public static void main(String[] args) throws IOException {
		for (int i = 0; i < NUMs; i++) {
			new Thread(new CountClient(12346)).start();
			count.countDown();
		}
	}
}
