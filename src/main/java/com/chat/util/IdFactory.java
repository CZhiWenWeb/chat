package com.chat.util;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 14:37
 * @UpdeteTime: 2020-06-17 14:37
 * @Description:
 */
public class IdFactory {
	public static int IDLEN = 14;

	public synchronized static long createId() {
		return Snow.createId();
	}

	static class Snow {
		private static long MAX_SEQ = ~(-1 << 4);   //同毫秒最多生成15个
		private static long SEQ = 0;
		private static long lastTime;

		public static long getTime() {
			return System.currentTimeMillis();
		}

		public static long getNextTime() {
			long next = System.currentTimeMillis();
			while (next <= lastTime)
				next = System.currentTimeMillis();
			return next;
		}

		public static long createId() {
			long time = getTime();
			if (time == lastTime) {
				SEQ = (SEQ + 1) & MAX_SEQ;
				if (SEQ == 0)   //序列号增值最大
					time = getNextTime();
			} else {
				SEQ = 0;
			}

			lastTime = time;

			return (time << 4)  //空出低四位
					| SEQ;      //放入seq
		}
	}

	public static void main(String[] args) {
		while (true) {
			System.out.println(IdFactory.createId());
		}
	}
}
