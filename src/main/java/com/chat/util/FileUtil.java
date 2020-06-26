package com.chat.util;


import com.chat.client.ClientSend;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
	private static BlockingQueue<Task> takeQue = new LinkedBlockingQueue<>(1000);
	private volatile static boolean open;
	private static Map<String, FileChannel> mapChannel = new HashMap<>();
	private static String dir;
	private static String type = ".txt";

	public static void addTake(byte[] bytes, int offset, int count) {
		takeQue.add(new Task(bytes, offset, count));
		System.out.println("addTake");
		synchronized (FileUtil.class) {     //保证只执行一个processor线程
			if (!open) {
				open = true;
				new Thread(() -> {
					try {
						processor();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}).start();
			}
		}
	}

	private static void processor() throws Exception {
		while (open) {
			Task task = takeQue.poll();
			while (task != null) {
				writeToFile(task.data, task.offset, task.count);
				task = takeQue.poll();
			}
			open = false;
			close();
		}
		System.out.println("end: " + takeQue.size());
	}

	private static void writeToFile(byte[] bytes, int offset, int count) throws Exception {
		String name;

		//name = new String(bytes, offset, IdFactory.IDLEN);   //以消息前缀命名

		name = new String(bytes, offset + count - IdFactory.IDLEN, IdFactory.IDLEN);     //以消息后缀命名

		System.out.println("writing: " + name);
		if (mapChannel.containsKey(name)) {
			if (mapChannel.get(name).isOpen()) {
				write(mapChannel.get(name), bytes, offset, count);
			} else {
				FileChannel fileChannel = openChannel(name);
				if (fileChannel != null) {
					write(fileChannel, bytes, offset, count);
					mapChannel.put(name, fileChannel);
				}
			}
		} else {
			FileChannel fileChannel = openChannel(name);
			if (fileChannel != null) {
				mapChannel.put(name, fileChannel);
				write(fileChannel, bytes, offset, count);
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

	private static void write(FileChannel channel, byte[] bytes, int offset, int count) throws IOException {
		if (byteBuffer == null)
			byteBuffer = ByteBuffer.allocate(cap);
		byteBuffer.put(bytes, offset, count);
		byteBuffer.flip();
		int num = channel.write(byteBuffer);
		byteBuffer.clear();
		if (count > num) {
			write(channel, bytes, offset + num, count - num);
		} else {
			byteBuffer.put(new byte[]{'\r', '\n'});
			byteBuffer.flip();
			channel.write(byteBuffer);
			byteBuffer.clear();
		}
	}

	//private static void writeByte(FileChannel channel, byte[] bytes, int head, int len) throws IOException {
	//	if (byteBuffer == null)
	//		byteBuffer = ByteBuffer.allocate(cap);
	//	byteBuffer.put(bytes, head, len);
	//	byteBuffer.flip();
	//	channel.write(byteBuffer);
	//	byteBuffer.clear();
	//}

	private static FileChannel openChannel(String name) {
		try {
			File file = new File(dir, name + type);

			return new FileOutputStream(file, true).getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		//Random random = new Random();
		//String[] strings = new String[10];
		//for (int i = 0; i < strings.length; i++) {
		//	strings[i] = Arrays.toString(random.ints(15, 0, 10).toArray());
		//}
		//
		//for (int i = 0; i < 10; i++) {
		//	new Thread(() -> add(strings)).start();
		//}
		System.out.println(FileUtil.findNums(dir));
	}

	//  测试方法
	private static void add(String[] strings) {
		for (String s : strings) {
			byte[] bytes = s.getBytes();
			FileUtil.addTake(bytes, 0, bytes.length);
		}
	}

	//查询文件数量，打印输入错误信息的文件名
	public static int findNums(String dir) throws IOException {
		File file = new File(dir);
		if (!file.isDirectory()) {
			return -1;
		}
		String[] names = file.list();
		File child;
		assert names != null;

		for (String name : names) {
			int num = 0;
			child = new File(dir, name);
			BufferedReader br = new BufferedReader(new FileReader(child));
			String s = br.readLine();
			while (s != null) {
				num++;
				s = br.readLine();
				if (s != null && s.length() != "好好学习tiantianxiangshang from:25485063612224".length()) {
					System.out.println(name);
					num = ClientSend.maxToNums;    //防止重复打印
					break;
				}
			}
			if (num != ClientSend.maxToNums)
				System.out.println(name);
		}
		return names.length;
	}

	private static class Task {
		byte[] data;
		int offset;
		int count;

		/**
		 * @param data   目标数组
		 * @param offset 开始位置
		 * @param count  偏移量
		 */
		Task(byte[] data, int offset, int count) {
			this.data = data;
			this.offset = offset;
			this.count = count;
		}
	}
}
