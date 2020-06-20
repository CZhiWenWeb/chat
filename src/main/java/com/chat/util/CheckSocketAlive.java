package com.chat.util;

import com.chat.cache.util.RedisClientTool;
import com.chat.client.Client;
import com.chat.socket.ReaderProcessor;
import com.chat.socket.SocketReader;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 16:32
 * @UpdeteTime: 2020-06-17 16:32
 * @Description:
 */
public class CheckSocketAlive implements Runnable {
	private CheckSocketAlive() {        //key根据时间生成，每次断开连接都会失效,清理无效key
		RedisClientTool clientTool = new RedisClientTool();
		Set set = clientTool.getAllValues(Client.redisKey);
		String[] strings = new String[set.size()];
		Iterator it = set.iterator();
		for (int i = 0; i < set.size(); i++)
			strings[i] = (String) it.next();
		clientTool.setDel(Client.redisKey, strings);
	}

	static Map<Long, SocketReader> map = ReaderProcessor.map;
	static ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	public static void start() {
		ses.scheduleAtFixedRate(create(), 0, 5, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		//long time = System.currentTimeMillis();
		//for (SocketReader socketReader : map.values()) {
		//	if (time - socketReader.lastTime > 50000) {
		//		try {
		//			socketReader.close();
		//			System.out.println(socketReader.socketId + ":close");
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
		//	}
		//}
		System.out.println("online:" + map.size());
	}

	private static CheckSocketAlive create() {
		return new CheckSocketAlive();
	}

	public static void main(String[] args) {
		CheckSocketAlive.start();
	}
}
