package com.chat.message;

import com.chat.cache.BufferBlockProxy;

import java.util.Queue;

/**
 * @Author: czw
 * @CreateTime: 2020-06-27 11:31
 * @UpdeteTime: 2020-06-27 11:31
 * @Description:
 */
public class NewWriteTask {
	public BufferBlockProxy msg;     //包含消息头消息体
	public Queue<BufferBlockProxy> ids;   //ids和标识符

	public NewWriteTask(BufferBlockProxy msg, Queue queue) {
		this.msg = msg;
		this.ids = queue;
	}
}
