package com.chat.cache.message;

import com.chat.socket.Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public interface MessageReader {
	void init(AllocateBuffer allocateBuffer);

	void read(Socket socket, ByteBuffer byteBuffer) throws IOException;

	List<Message> getMessage();
}
