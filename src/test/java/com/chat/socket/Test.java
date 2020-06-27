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

	public static void main(String[] args) {
		Runnable runnable = () -> {
			try {
				count.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(A.getInstance());
		};

		for (int i = 0; i < 5; i++) {
			new Thread(runnable).start();
			count.countDown();
		}
	}
}

class A {
	private A() {
		System.out.println("init");
	}

	static A getInstance() {
		return Holder.a;
	}

	static class Holder {
		static A a = new A();
	}
}
