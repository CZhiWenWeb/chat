package com.chat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Author: czw
 * @CreateTime: 2020-06-19 16:29
 * @UpdeteTime: 2020-06-19 16:29
 * @Description:
 */
public class FileUtil {
	private static int cap = 1024;
	private FileChannel channel;
	private ByteBuffer byteBuffer;
	private File afile;

	public FileUtil(String name) throws FileNotFoundException {
		this.byteBuffer = ByteBuffer.allocate(cap);
		this.afile = new File(name);
	}

	public int write(byte[] bytes) throws IOException {
		FileOutputStream out = new FileOutputStream(afile, true);
		this.channel = out.getChannel();    //小文件没必要

		int len = bytes.length;
		int start = 0;
		while (len > cap) {
			write(bytes, start, cap);
			len -= cap;
			start += cap;
		}
		write(bytes, start, len);

		write(new byte[]{'\r', '\n'}, 0, 2);

		channel.close();
		out.close();

		return bytes.length;
	}

	private void write(byte[] bytes, int head, int len) throws IOException {
		byteBuffer.put(bytes, head, len);
		byteBuffer.flip();
		channel.write(byteBuffer);
		byteBuffer.clear();
	}

	public static void main(String[] args) throws IOException {
		FileUtil.cap = 8;
		FileUtil fileWriter = new FileUtil("test");
		FileUtil fileWriter1 = new FileUtil("test");
		byte[] bytes = "hello world".getBytes();
		fileWriter1.write(bytes);
	}
}
