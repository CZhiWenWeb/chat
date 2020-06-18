package com.chat.client;

import com.chat.util.IdFactory;

/**
 * @Author: czw
 * @CreateTime: 2020-06-18 09:36
 * @UpdeteTime: 2020-06-18 09:36
 * @Description: 格式：length sendId to acceptorId enum(1,0) +body
 * 长度为头10位编码，即最大不超过 2^10-1  = 1023；sendId长度为14;所以头部长为10+14+2+14+1
 * enum为0标识acceptorId结束；为1标识需继续向body读取15位获取acceptorId，直到标识为0(多人消息)
 */
public class Message {
	private byte[] datas;
	public static int len = 10;
	private static int id = IdFactory.IDLEN;
	public static int startIndex = len + id * 2 + 3;

	public void initMsg(String msg, String header) throws Exception {
		if (header.length() > startIndex)
			throw new Exception("错误格式");
		if (startIndex + msg.length() > Math.pow(2, len) - 1)
			throw new Exception("消息过长");
		datas = new byte[startIndex + msg.length()];

		System.arraycopy(msg.getBytes(), 0, datas, startIndex, msg.length());

		System.arraycopy(header.getBytes(), 0, datas, startIndex - header.length(), header.length());
	}

	public byte[] getMsg() {
		return datas;
	}

	public static void main(String[] args) {
		byte[] bytes = new byte[2];
		System.out.println();
	}
}
