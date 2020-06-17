package com.chat.socket;

import com.chat.client.IdFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:29
 * @UpdeteTime: 2020-06-17 11:29
 * @Description:
 */
public class SocketReader implements Closeable {
	public SocketChannel sc;
	public ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
	public static Map<Long, SocketReader> map = ReaderProcessor.map;
	public long socketId;
	public long lastTime;

	public SocketReader(SocketChannel sc) {
		this.sc = sc;
	}

	public int read() throws IOException {
		int i = sc.read(byteBuffer);
		byteBuffer.flip();
		if (i > 0) {
			byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);
			long id = Long.valueOf(new String(bytes, 0, IdFactory.IDLEN));
			socketId = id;
			lastTime = System.currentTimeMillis();
			map.put(id, this);
			System.out.println(id + ":get");
			byteBuffer.clear();
		} else {
			lastTime = System.currentTimeMillis();
		}
		return i;
	}

	@Override
	public void close() throws IOException {
		map.remove(socketId);
		sc.close();
	}
}
