package com.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: czw
 * @CreateTime: 2020-06-08 09:22
 * @UpdeteTime: 2020-06-08 09:22
 * @Description:
 */
public class Hello {
	public static void main(String[] args) throws IOException, InterruptedException {
		//ChatClient cc=new ChatClient();
		//cc.run();
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(12346));
		ExecutorService es = Executors.newFixedThreadPool(3);
		for (int i = 0; i < 3; i++) {
			Runnable r = new Run(selectorFactory(ssc));
			es.submit(r);
		}
		ssc.accept();
		Thread.sleep(3000);
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(new InetSocketAddress("127.0.0.1", 12346));
	}

	static private Selector selectorFactory(ServerSocketChannel ssc) throws IOException {
		Selector result = Selector.open();
		ssc.register(result, SelectionKey.OP_ACCEPT);
		return result;
	}

	static class Run implements Runnable {
		private Selector selector;

		public Run(Selector selector) {
			this.selector = selector;
		}

		@Override
		public void run() {
			try {
				System.out.println(selector.toString() + "running");
				while (!Thread.interrupted()) {
					int i = selector.select();
					if (i > 0)
						System.out.println(selector.toString() + ":" + i);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
