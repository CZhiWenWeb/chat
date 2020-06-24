package com.chat.message;

import com.chat.util.CodeUtil;
import com.chat.util.FileUtil;
import com.chat.util.IdFactory;

/**
 * @Author: czw
 * @CreateTime: 2020-06-24 10:16
 * @UpdeteTime: 2020-06-24 10:16
 * @Description:
 */
public class ParseMsgFromServer {

	public static void printMsg(byte[] bytes) {
		try {
			int len = CodeUtil.bytesToInt(bytes, 0, Message.len);
			String from = new String(bytes, Message.len, IdFactory.IDLEN);
			String msg = new String(bytes, Message.startIndex, len - Message.startIndex);
			System.out.println(msg + ":from" + from);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeToFile(byte[] bytes) {
		FileUtil.addTake(bytes, 0, bytes.length);
	}
}
