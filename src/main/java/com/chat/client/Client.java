package com.chat.client;

import java.io.IOException;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:46
 * @UpdeteTime: 2020-06-17 11:46
 * @Description:
 */
public class Client implements Runnable {
	public static String redisKey = "clientId";
	private ClientAcceptor acceptor;
	private ClientSend send;

	public Client(int port) throws IOException {
		send = new ClientSend(port);
		acceptor = new ClientAcceptor(send.sc);
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
