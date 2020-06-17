package com.chat.socket;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:51
 * @UpdeteTime: 2020-06-17 10:51
 * @Description:
 */
public class Server implements Runnable {
	private int port;

	private SocketAccepter accepter;
	private ReaderProcessor reader;
	private WriterProcessor writerProcessor;

	public Server(int port) {
		this.port = port;
		this.accepter = new SocketAccepter(port);

		this.reader = new ReaderProcessor(accepter.socketQue);

		this.writerProcessor = new WriterProcessor(reader.outboundSocket);
	}

	@Override
	public void run() {


		new Thread(accepter).start();

		new Thread(reader).start();

		new Thread(writerProcessor).start();
	}

	public static void main(String[] args) {
		Server server = new Server(12346);
		server.run();
	}
}
