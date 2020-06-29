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
		a.hhh();
		new Thread(()->a.ddd()).start();
		//a.ddd();
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
