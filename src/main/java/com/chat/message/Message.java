package com.chat.message;

import com.chat.util.IdFactory;

import java.nio.charset.StandardCharsets;

/**
 * @Author: czw
 * @CreateTime: 2020-06-18 09:36
 * @UpdeteTime: 2020-06-18 09:36
 * @Description: 消息格式：lengthB sendId + body + lengthA acceptorId
 * 服务端解析规则：
 * header+body长度为头10位表示，即最大不超过 2^10-1  = 1023；sendId长度为14;所以头部长为10+14
 * lengthA记录acceptorId长度，长度为10个单位，最大同为1023；
 * 客户端解析规则：
 * sendId为发件人，body为信息，acceptorId为空
 */
public class Message {
	private byte[] datas;
	public static int len = 10;
	private static int id = IdFactory.IDLEN;
	public static int startIndex = len + id;
	public boolean isComplete;

	/**
	 * @param body
	 * @param send
	 * @param to   to数组不能有元素为空(String[] ex=new String[5];   ex有五个元素为空)
	 * @throws Exception
	 */
	public void initMsg(String body, String send, String... to) throws Exception {
		if (isComplete) {
			throw new Exception("禁止重复初始化");
		}
		int bodyLen = body.getBytes(StandardCharsets.UTF_8).length;
		if (startIndex + bodyLen > (1 << len) - 1)
			throw new Exception("消息过长");
		String binaryLenB = Integer.toBinaryString(body.getBytes().length + startIndex);

		int lengthA = to.length * (IdFactory.IDLEN) + len;
		if (lengthA > ((1 << len) - 1))
			throw new Exception("to过长");
		datas = new byte[startIndex + bodyLen +     //头部+body需要的内存
				lengthA];    //接收Id需要的内存

		//System.arraycopy(msg.getBytes(StandardCharsets.UTF_8), 0, datas, startIndex, bodyLen);
		byte[] header = (binaryLenB + send).getBytes(StandardCharsets.UTF_8);
		System.arraycopy(header, 0, datas, startIndex - header.length, header.length);      //头部写入

		System.arraycopy(body.getBytes(StandardCharsets.UTF_8), 0, datas, startIndex, bodyLen);     //body写入

		String binaryLenA = Integer.toBinaryString(lengthA);
		byte[] byteLenA = binaryLenA.getBytes(StandardCharsets.UTF_8);
		int next = body.getBytes().length + startIndex;
		System.arraycopy(byteLenA, 0, datas, next + (len - byteLenA.length), byteLenA.length);
		next += len;
		for (String s : to) {
			System.arraycopy(s.getBytes(StandardCharsets.UTF_8), 0, datas, next, IdFactory.IDLEN);
			next += IdFactory.IDLEN;
		}
		isComplete = true;
	}

	public void initMsgWithOutAccIds(byte[] bytes) throws Exception {
		if (isComplete) {
			throw new Exception("禁止重复初始化");
		}
		datas = new byte[bytes.length + len];
		System.arraycopy(bytes, 0, datas, 0, bytes.length);
		String binaryLen = Integer.toBinaryString(len);
		byte[] byteLen = binaryLen.getBytes();
		System.arraycopy(byteLen, 0, datas, bytes.length + (len - byteLen.length), byteLen.length);
	}

	public byte[] getMsg() {
		return datas;
	}

	public int len() {
		return datas.length;
	}

	public static void main(String[] args) {
		byte[] bytes = new byte[2];
		System.out.println();
	}
}
