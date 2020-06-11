package com.chat;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * @Author: czw
 * @CreateTime: 2020-06-11 14:08
 * @UpdeteTime: 2020-06-11 14:08
 * @Description:
 */
public class DomainFactory {

	public static Selector openSelector(ServerSocketChannel ssc, int interest) throws IOException {
		Selector selector = Selector.open();
		ssc.register(selector, interest);
		return selector;
	}


}
