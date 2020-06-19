package com.chat.socket;

import com.chat.cache.util.RedisClientTool;
import com.chat.client.Client;
import com.chat.client.Message;
import com.chat.util.CodeUtil;
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
	public ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
	public ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
	public long socketId;
	public long lastTime;
	private Selector readSelector;
	private int msgId = 0;
	private int msgLen = -1;    //消息剩余长度
	private int nextIndex = 0;  //读取下个元素的指针位置
	private int writeIndex = 0; //写入下个元素的指针位置
	private byte[] completeMsg; //存放完整消息的数组
	public Map<Integer, byte[]> msgMap = new HashMap<>();

	public Map<Integer, Queue<Long>> msgToIdsMap = new HashMap<>();

	private RedisClientTool redisClientTool = new RedisClientTool();

	public SocketReader(SocketChannel sc, Selector readSelector)  {
		this.sc = sc;
		this.readSelector = readSelector;
	}

	public int read() throws Exception {
		int i = sc.read(byteBuffer);
		byteBuffer.flip();
		if (i > 0) {
			System.out.println(i);
			Queue<Long> toIdsQue = new LinkedList<>();
			byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);
			if (new String(bytes).contains("login")) {
				long id = Long.parseLong(new String(bytes, nextIndex, IdFactory.IDLEN));
				map.putIfAbsent(id, this);
				socketId = id;
				redisClientTool.setAdd(Client.redisKey, String.valueOf(id));
				System.out.println(id + ":get");
				nextIndex = 0;
				return i;
			}
			if (msgLen == -1) {
				msgLen = CodeUtil.binaryStringToInt(new String(bytes, nextIndex, Message.len)); //获取消息长度
				completeMsg = new byte[msgLen];
				msgLen -= i;
				System.arraycopy(bytes, 0, completeMsg, writeIndex, i);
				writeIndex += i;
				return i;
			}
			if (msgLen > 0) {   //获取完整消息
				msgLen -= i;
				System.arraycopy(bytes, 0, completeMsg, writeIndex, i);
				writeIndex += i;
				if (msgLen > 0)
					return i;
			}
			msgLen = completeMsg.length;    //获取完整消息长度
			bytes = completeMsg;
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
			msgMap.put(msgId, temp);  //存放msg
			msgToIdsMap.put(msgId++, toIdsQue);   //需发送的ids
			byteBuffer.clear();

			reset();
		} else {
			lastTime = System.currentTimeMillis();
		}
		return i;
	}

	private void reset() {
		msgLen = -1;
		writeIndex = 0;
		nextIndex = 0;
		completeMsg = null;
	}

	@Override
	public void close() throws IOException {
		sc.keyFor(readSelector).cancel();
		sc.close();
		map.remove(socketId);
	}
}