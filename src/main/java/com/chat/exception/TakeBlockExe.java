package com.chat.exception;

/**
 * @Author: czw
 * @CreateTime: 2020-06-12 16:33
 * @UpdeteTime: 2020-06-12 16:33
 * @Description:
 */
public class TakeBlockExe extends RuntimeException {
	public TakeBlockExe() {
		super();
	}

	public TakeBlockExe(String msg) {
		super(msg);
	}
}
