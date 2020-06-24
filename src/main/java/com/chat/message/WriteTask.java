package com.chat.message;

import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-24 09:16
 * @UpdeteTime: 2020-06-24 09:16
 * @Description:
 */
public class WriteTask {
	public byte[] msg;      //包含消息头和body
	public Queue<byte[]> accIds;    //包含id和结束标识符

	public WriteTask(byte[] msg, Queue accIds) {
		this.msg = msg;
		this.accIds = accIds;
	}
}
