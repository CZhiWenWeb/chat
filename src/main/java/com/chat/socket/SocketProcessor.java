package com.chat.socket;

import com.chat.cache.message.AllocateBuffer;
import com.chat.cache.message.Message;
import com.chat.cache.message.MessageReaderFactory;
import com.chat.cache.message.MessageWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;

/**
 * @Author: czw
 * @CreateTime: 2020-06-13 11:38
 * @UpdeteTime: 2020-06-13 11:38
 * @Description:
 */
public class SocketProcessor implements Runnable {
	private Queue<Socket> inboundSC;
	private AllocateBuffer buffer;
	private Map<Long, Socket> socketMap = new HashMap<>();
	//需》=最大消息内存
	private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024 * 1024);
	private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024 * 1024);

	private Selector readSelector;
	private Selector writeSelector;

	private long nextSocketId = 16 * 1024;

	private MessageReaderFactory readerFactory;

	private Set<Socket> emptyToNonEmptySockets = new HashSet<>();
	private Set<Socket> nonEmptyToEmptySockets = new HashSet<>();

	private boolean stop;

	public SocketProcessor(Queue<Socket> inboundSocketQue, AllocateBuffer buffer, MessageReaderFactory readerFactory) throws IOException {
		this.inboundSC = inboundSocketQue;
		this.buffer = buffer;

		this.readSelector = Selector.open();
		this.writeSelector = Selector.open();

		this.readerFactory = readerFactory;
	}

	@Override
	public void run() {
		while (!stop) {

		}
	}


	private void takeNewSockets() throws IOException {
		//非阻塞操作，从队列获取socket，队列为空返回null
		Socket newSocket = inboundSC.poll();

		while (newSocket != null) {
			newSocket.socketId = nextSocketId++;
			newSocket.sc.configureBlocking(false);
			//为socket分配信息读取缓存
			newSocket.reader = this.readerFactory.createReader(this.buffer);
			newSocket.reader.init(buffer);

			//需要写入的信息队列
			newSocket.writer = new MessageWriter();

			this.socketMap.put(newSocket.socketId, newSocket);

			//向readSelector注册read事件
			SelectionKey key = newSocket.sc.register(readSelector, SelectionKey.OP_READ);
			key.attach(newSocket);

			newSocket = inboundSC.poll();
		}
	}

	private void readFormSockets() throws IOException {
		//非阻塞操作，立即返回就绪事件个数
		int readReady = readSelector.selectNow();

		if (readReady > 0) {
			Set<SelectionKey> set;
			for (SelectionKey key : set = readSelector.selectedKeys())
				readFormSocket(key);
			set.clear();
		}
	}

	private void readFormSocket(SelectionKey key) throws IOException {
		Socket socket = (Socket) key.attachment();
		//从socket读取信息至待写入信息队列
		socket.reader.read(socket, readByteBuffer);

		//获取读取到的完整信息
		List<Message> fullMessage = socket.reader.getMessage();
		if (fullMessage.size() > 0) {
			for (Message message : fullMessage) {
				//message.socketId
			}
			fullMessage.clear();
		}

		if (socket.endOfStreamRea) {
			System.out.println("Socket closed:" + socket.socketId);
			this.socketMap.remove(socket.socketId);
			key.attach(null);   //帮助绑定实例被回收
			key.channel();  //取消key
			key.channel().close();  //关闭channel
		}
	}

	public void writeToSockets(){

	}
}

