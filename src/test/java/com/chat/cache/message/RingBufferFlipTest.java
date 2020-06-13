package com.chat.cache.message;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: czw
 * @CreateTime: 2020-06-11 16:48
 * @UpdeteTime: 2020-06-11 16:48
 * @Description:
 */
public class RingBufferFlipTest {

	@Test
	public void test() {

	}

	public static void main(String[] args) {
		RingBufferFlip bufferFlip = new RingBufferFlip(48);
		for (int i = 0; i < 48; i++)
			bufferFlip.put((byte) i);
		ExecutorService es = Executors.newFixedThreadPool(2);
		Runnable r = () -> {
			while (true) {
				System.out.println(bufferFlip.take());
			}
		};
		for (int i = 0; i < 3; i++)
			es.submit(r);
	}
}
