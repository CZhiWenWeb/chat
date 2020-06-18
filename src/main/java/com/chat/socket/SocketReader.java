package com.chat.socket;

import com.chat.cache.util.RedisClientTool;
import com.chat.client.Client;
import com.chat.client.Message;
import com.chat.util.IdFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:29
 * @UpdeteTime: 2020-06-17 11:29
 * @Description:
 */
public class SocketReader implements Closeable {
	public static Map map = ReaderProcessor.map;
	public SocketChannel sc;
	public ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
	public long socketId;
	public long lastTime;
	private Selector readSelector;
	private int msgId = 0;

	public Map<Integer, byte[]> msgMap = new HashMap<>();

	public Map<Integer, Queue<Long>> msgToIdsMap = new HashMap<>();

	private RedisClientTool redisClientTool = new RedisClientTool();

	public SocketReader(SocketChannel sc, Selector readSelector) {
		this.sc = sc;
		this.readSelector = readSelector;
	}

	public int read() throws IOException {
		int i = sc.read(byteBuffer);
		byteBuffer.flip();
		if (i > 0) {
			Queue<Long> toIdsQue = new LinkedList<>();
			byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);
			int nextIndex = 0;  //下个元素的指针位置
			if (new String(bytes).contains("login")) {
				long id = Long.parseLong(new String(bytes, nextIndex, IdFactory.IDLEN));
				map.putIfAbsent(id, this);
				socketId = id;
				redisClientTool.setAdd(Client.redisKey, String.valueOf(id));
				System.out.println(id + ":get");
				return i;
			}
			int msgLen = Integer.parseInt(new String(bytes, nextIndex, Message.len));
			nextIndex += Message.len;
			long id = Long.parseLong(new String(bytes, nextIndex, IdFactory.IDLEN));
			nextIndex += IdFactory.IDLEN;
			socketId = id;
			if (bytes[nextIndex++] == 't' && bytes[nextIndex++] == 'o') {
				boolean end = false;
				while (!end) {
					Long toId = Long.valueOf(new String(bytes, nextIndex, IdFactory.IDLEN));
					nextIndex += IdFactory.IDLEN;
					toIdsQue.offer(toId);
					if (bytes[nextIndex++] == '0') {    //0为结束，1继续添加id
						end = true;
					}
				}
			}
			byte[] temp = new byte[msgLen - nextIndex];
			System.arraycopy(bytes, nextIndex, temp, 0, msgLen - nextIndex);
			msgMap.put(msgId++, temp);  //存放msg
			msgToIdsMap.put(msgId, toIdsQue);   //需发送的ids
			byteBuffer.clear();
		} else {
			lastTime = System.currentTimeMillis();
		}
		return i;
	}

	@Override
	public void close() throws IOException {
		sc.keyFor(readSelector).cancel();
		sc.close();
		map.remove(socketId);
	}
}
