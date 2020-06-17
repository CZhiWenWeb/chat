package com.chat.client;

import java.io.IOException;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:46
 * @UpdeteTime: 2020-06-17 11:46
 * @Description:
 */
public class Client implements Runnable {
	private MsgAcceptor acceptor;
	private MsgSend send;

	public Client(int port) throws IOException {
		send = new MsgSend(port);
		acceptor = new MsgAcceptor(send.sc);
	}

	@Override
	public void run() {
		new Thread(send).start();
		new Thread(acceptor).start();
	}

	public static void main(String[] args) throws IOException {
		Client client = new Client(12346);
		client.run();
	}
}
