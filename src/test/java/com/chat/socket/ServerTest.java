package com.chat.socket;

import java.io.IOException;

/**
 * @Author: czw
 * @CreateTime: 2020-06-16 11:31
 * @UpdeteTime: 2020-06-16 11:31
 * @Description:
 */
public class ServerTest {
	public static void main(String[] args) throws IOException {
		Server server = new Server(12346);
		server.run();
	}
}
