package com.chat.client;

import com.chat.message.MessageAccept;
import com.chat.message.ParseMsgFromServer;
import com.chat.message.WriteTask;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:36
 * @UpdeteTime: 2020-06-17 11:36
 * @Description:
 */
public class ClientAcceptor implements Runnable {
	public static AtomicInteger integer = new AtomicInteger();
	private Selector readSelector;
	private MessageAccept messageAccept;

	public ClientAcceptor(SocketChannel sc) throws IOException {
		this.readSelector = Selector.open();
		SelectionKey key = sc.register(readSelector, SelectionKey.OP_READ);
		this.messageAccept = new MessageAccept(sc);
		key.attach(messageAccept);
	}

	@Override
	public void run() {
		while (true) {
			try {
				int i = readSelector.selectNow();
				if (i > 0) {
					Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
					for (SelectionKey key : selectionKeys) {
						MessageAccept accept = (MessageAccept) key.attachment();
						accept.read();
						WriteTask task = accept.tasks.poll();
						while (task != null) {
							integer.incrementAndGet();
							ParseMsgFromServer.printMsg(task.msg);  //消息打印
							System.out.println("累计接收信息：" + integer);
							//ParseMsgFromServer.writeToFile(task.msg);   //消息存储
							task = accept.tasks.poll();
						}
					}
					selectionKeys.clear();
				}
				Thread.sleep(1000);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("解码失败");
				e.printStackTrace();
			}
		}
	}
}
