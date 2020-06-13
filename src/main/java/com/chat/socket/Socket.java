package com.chat.socket;

import com.chat.cache.message.MessageReader;
import com.chat.cache.message.MessageWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Author: czw
 * @CreateTime: 2020-06-13 10:32
 * @UpdeteTime: 2020-06-13 10:32
 * @Description:
 */
public class Socket {

	public SocketChannel sc;
	//直至第一次使用时初始化
	public MessageReader reader;
	public MessageWriter writer;
	public long socketId;
	public boolean endOfStreamRea;

	public Socket(SocketChannel sc) {
		this.sc = sc;

	}

	//传入的bf需为读就绪
	public int read(ByteBuffer byteBuffer) throws IOException {
		int bytesRead = sc.read(byteBuffer);
		int tatalBytesRead = bytesRead;
		while (bytesRead > 0) {
			bytesRead = this.sc.read(byteBuffer);
			tatalBytesRead += bytesRead;
		}

		if (bytesRead == -1)
			this.endOfStreamRea = true;

		return tatalBytesRead;
	}

	//bf需为写就绪
	public int write(ByteBuffer byteBuffer) throws IOException {
		int bytesWritten = this.sc.write(byteBuffer);
		int totalBytesWritten = bytesWritten;

		while (bytesWritten > 0 && byteBuffer.hasRemaining()) {
			bytesWritten = this.sc.write(byteBuffer);
			totalBytesWritten += bytesWritten;
		}

		return totalBytesWritten;
	}
}
