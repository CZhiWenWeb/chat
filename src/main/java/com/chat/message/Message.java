package com.chat.message;

import com.chat.cache.BufferBlock;
import com.chat.cache.BufferRing;
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
	private static BufferRing bufferRing = BufferRing.getInstance();
	private BufferBlock datas;
	public static int len = 10;
	private static int id = IdFactory.IDLEN;
	public static int startIndex = len + id;
	public boolean isComplete;

	/**
	 * @param body
	 * @param send
	 * @param to   to数组不能有元素为空(String[] ex=new String[5];   ex有五个元素为空)且to[i].len=ID.len
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

		datas = bufferRing.dispatcher(startIndex + bodyLen +    //头部+body需要的内存
				lengthA);   //接收Id需要的内存

		byte[] header = (binaryLenB + send).getBytes(StandardCharsets.UTF_8);
		byte[] zeroBeforeH = fillZero(startIndex - header.length);
		datas.readFromBytes(zeroBeforeH);   //头部长度不足填充0
		datas.readFromBytes(header);      //头部写入
		datas.readFromBytes(body.getBytes(StandardCharsets.UTF_8));          //body写入
		String binaryLenA = Integer.toBinaryString(lengthA);
		byte[] byteLenA = binaryLenA.getBytes(StandardCharsets.UTF_8);
		byte[] zeroBeforeA = fillZero(len - byteLenA.length);
		datas.readFromBytes(zeroBeforeA);   //头部长度不足填充0
		datas.readFromBytes(byteLenA);   //len写入
		for (String s : to) {
			datas.readFromBytes(s.getBytes(StandardCharsets.UTF_8));
		}
		isComplete = true;
	}

	public BufferBlock getMsg() {
		return datas;
	}

	private byte[] fillZero(int len) {
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i++)
			bytes[i] = '0';
		return bytes;
	}
	public static void main(String[] args) {
		byte[] bytes = new byte[2];
		System.out.println();
	}
}
