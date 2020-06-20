package com.chat.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-19 16:29
 * @UpdeteTime: 2020-06-19 16:29
 * @Description:
 */
public class FileUtil {

	private FileUtil() {

	}

	static {
		File file = new File("msg");
		if (!file.exists()) {
			boolean b = file.mkdir();
			if (!b) {
				try {
					throw new Exception("创建目录失败");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		dir = file.getPath();
	}
	private static int cap = 1024;
	private static ByteBuffer byteBuffer;
	private static BlockingQueue<byte[]> takeQue = new LinkedBlockingQueue<>(1000);
	private volatile static boolean open;
	private static Map<String, FileChannel> mapChannel = new HashMap<>();
	private static String dir;
	private static String type = ".txt";

	public static void addTake(byte[] bytes) {
		takeQue.add(bytes);
		System.out.println("addTake");
		synchronized (FileUtil.class) {     //保证只执行一个processor线程
			if (!open) {
				open = true;
				new Thread(() -> {
					try {
						processor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}).start();
			}
		}
	}

	private static void processor() throws IOException, InterruptedException {
		while (open) {
			byte[] bytes = takeQue.poll();
			while (bytes != null) {
				writeToFile(bytes);
				bytes = takeQue.poll();
			}
			open = false;
			close();
		}
		System.out.println("end: " + takeQue.size());
	}

	private static void writeToFile(byte[] bytes) throws IOException {
		String name = new String(bytes, bytes.length - 14, 14);
		System.out.println("writing: " + name);
		if (mapChannel.containsKey(name)) {
			if (mapChannel.get(name).isOpen()) {
				write(mapChannel.get(name), bytes);
			} else {
				FileChannel fileChannel = openChannel(name);
				if (fileChannel != null) {
					write(fileChannel, bytes);
					mapChannel.put(name, fileChannel);
				}
			}
		} else {
			FileChannel fileChannel = openChannel(name);
			if (fileChannel != null) {
				mapChannel.put(name, fileChannel);
				write(fileChannel, bytes);
			}
		}
	}

	private static void close() throws IOException {
		for (String name : mapChannel.keySet()) {
			if (!open) {
				mapChannel.get(name).close();
			} else {
				return;
			}
		}
		if (!open)
			byteBuffer = null;
	}

	private static void write(FileChannel channel, byte[] bytes) throws IOException {
		int len = bytes.length;
		int start = 0;
		while (len > cap) {
			write(channel, bytes, start, cap);
			start += cap;
			len -= cap;
		}
		write(channel, bytes, start, len);
		write(channel, new byte[]{'\r', '\n'}, 0, 2);
	}

	private static void write(FileChannel channel, byte[] bytes, int head, int len) throws IOException {
		if (byteBuffer == null)
			byteBuffer = ByteBuffer.allocate(cap);
		byteBuffer.put(bytes, head, len);
		byteBuffer.flip();
		channel.write(byteBuffer);
		byteBuffer.clear();
	}

	private static FileChannel openChannel(String name) {
		try {
			return new FileOutputStream(new File(dir, name + type), true).getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		Random random = new Random();
		String[] strings = new String[10];
		for (int i = 0; i < strings.length; i++) {
			strings[i] = Arrays.toString(random.ints(15, 0, 10).toArray());
		}

		for (int i = 0; i < 10; i++) {
			new Thread(() -> add(strings)).start();
		}

	}

	private static void add(String[] strings) {
		for (String s : strings) {
			byte[] bytes = s.getBytes();
			FileUtil.addTake(bytes);
		}
	}
}
