package com.chat.client;

import com.chat.util.CodeUtil;
import com.chat.util.FileUtil;
import com.chat.util.IdFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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
						System.out.println(new String(bytes, len - 14, 14));
						msgHander(bytes);       //记录消息，粘包处理
						//FileUtil.addTake(bytes,0,bytes.length);        //记录消息
						//System.out.println(new String(bytes));
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

	private void msgHander(byte[] source) throws Exception {   //粘包处理
		byte[] target = "from:".getBytes(StandardCharsets.UTF_8);
		int len = target.length + IdFactory.IDLEN;
		int lenSource = source.length;
		int start = 0;
		int count;
		while (true) {
			int index = CodeUtil.findBytesByBM(source, start, lenSource,
					target, 0, target.length);
			if (index == -1)
				break;
			count = CodeUtil.findBytesByBM(source, start, lenSource,
					target, 0, target.length) + len;
			FileUtil.addTake(source, start, count - start);
			start = count;
		}
	}
}
