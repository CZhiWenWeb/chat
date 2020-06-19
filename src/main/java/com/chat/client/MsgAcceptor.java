package com.chat.client;

import com.chat.util.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @Author: czw
 * @CreateTime: 2020-06-17 11:36
 * @UpdeteTime: 2020-06-17 11:36
 * @Description:
 */
public class MsgAcceptor implements Runnable {

	private Selector readSelector;
	private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);

	public MsgAcceptor(SocketChannel sc) throws IOException {
		this.readSelector = Selector.open();
		sc.register(readSelector, SelectionKey.OP_READ);
	}

	@Override
	public void run() {
		while (true) {
			try {
				int i = readSelector.selectNow();
				if (i > 0) {
					Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
					for (SelectionKey key : selectionKeys) {
						SocketChannel sc = (SocketChannel) key.channel();
						sc.read(byteBuffer);
						byteBuffer.flip();
						byte[] bytes = new byte[byteBuffer.remaining()];
						byteBuffer.get(bytes);
						byteBuffer.clear();
						int len = bytes.length;
						new FileUtil(new String(bytes, len - 15, 14)).write(bytes);
						//System.out.println(new String(bytes));
					}
					selectionKeys.clear();
				}
				Thread.sleep(1000);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
