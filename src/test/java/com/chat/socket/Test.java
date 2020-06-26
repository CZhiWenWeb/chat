package com.chat.socket;

import com.chat.client.CountClient;

import java.io.IOException;

/**
 * @Author: czw
 * @CreateTime: 2020-06-26 18:44
 * @UpdeteTime: 2020-06-26 18:44
 * @Description:
 */
public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Server server = new Server(12346);
		CountClient.test();

	}
}
