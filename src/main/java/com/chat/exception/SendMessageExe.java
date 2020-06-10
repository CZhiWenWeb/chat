package com.chat.exception;

import java.io.IOException;

/**
 * @Author: czw
 * @CreateTime: 2020-06-10 09:05
 * @UpdeteTime: 2020-06-10 09:05
 * @Description:
 */
public class SendMessageExe extends IOException {
	public SendMessageExe() {
		super();
	}

	public SendMessageExe(String msg) {
		super(msg);
	}
}
