package com.chat.socket;

import java.util.concurrent.*;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 10:51
 * @UpdeteTime: 2020-06-17 10:51
 * @Description:
 */
public class Server implements Runnable {
	private static final int readNums = 2;
	private static final int writeNums = 2;
	private ExecutorService esReader = Executors.newFixedThreadPool(readNums);
	private ExecutorService esWriter = Executors.newFixedThreadPool(writeNums);
	private SocketAccepter accepter;
	//private ReaderProcessor reader;
	//private WriterProcessor writerProcessor;
	private BlockingQueue readerQueue = new LinkedBlockingQueue<>();      //线程安全的写消息队列
	public static ConcurrentMap mapOnLine = new ConcurrentHashMap();
	public Server(int port) {
		this.accepter = new SocketAccepter(port);
		//this.reader = new ReaderProcessor(accepter.socketQue, readerQueue);
		//this.writerProcessor = new WriterProcessor(reader.outboundSocket);
	}

	@Override
	public void run() {

		new Thread(accepter).start();

		for (int i = 0; i < readNums; i++) {
			esReader.submit(new ReaderProcessor(accepter.socketQue, readerQueue));
		}
		for (int i = 0; i < writeNums; i++) {
			esWriter.submit(new WriterProcessor(readerQueue, mapOnLine));
		}
		//
		//new Thread(reader).start();
		//
		//new Thread(writerProcessor).start();
	}

	public static void main(String[] args) {
		Server server = new Server(12346);
		server.run();
	}
}
