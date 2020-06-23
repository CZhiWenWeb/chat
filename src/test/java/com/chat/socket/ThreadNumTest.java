package com.chat.socket;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 17:04
 * @UpdeteTime: 2020-06-17 17:04
 * @Description:
 */
public class ThreadNumTest {
	public static void main(String[] args) {
		Queue queue = new LinkedList();
		System.out.println(queue.poll());
		//Run run = new Run();
		//new Thread(() -> {
		//	new Thread(() -> {
		//		new Thread(run)
		//				.start();
		//		run.run();
		//	}).start();
		//	run.run();
		//}).start();
	}

	static class Run implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
