package com.chat.cache.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author: czw
 * @CreateTime: 2020-06-09 09:38
 * @UpdeteTime: 2020-06-09 09:38
 * @Description:
 */
public class ScheduledUtil {
	static ScheduledExecutorService ses;
	static int i;


	public synchronized static ScheduledFuture start(Runnable r, int firt, int seconds) throws Exception {
		if (i == 2) {
			throw new Exception("任务上限");
		}
		if (ses == null) {
			ses = Executors.newScheduledThreadPool(2);
			i = 1;
		}
		return ses.scheduleAtFixedRate(r, firt, seconds, TimeUnit.SECONDS);
	}


	public static void main(String[] args) throws Exception {
		Runnable r = () -> System.out.println("hi");
		ScheduledFuture sf = ScheduledUtil.start(r, 5, 2);
		Thread.sleep(10000);
		sf.cancel(true);
		while (!Thread.interrupted()) {
			if (sf.isCancelled()) {
				ses.shutdown();
				break;
			}
		}
	}
}
