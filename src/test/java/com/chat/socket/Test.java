package com.chat.socket;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: czw
 * @CreateTime: 2020-06-26 18:44
 * @UpdeteTime: 2020-06-26 18:44
 * @Description:
 */
public class Test {
	static CountDownLatch count = new CountDownLatch(5);

	public static void main(String[] args) throws InterruptedException {
		A a=new A();
		new Thread(() -> {
			try {
				a.hhh();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		Thread.sleep(100);
		synchronized (a) {
			a.ddd();
		}
	}
}

class A {
	public synchronized void ddd() {
		System.out.println("ddd");
	}

	public synchronized void hhh() throws InterruptedException {
		Thread.sleep(5000);
	}
}
