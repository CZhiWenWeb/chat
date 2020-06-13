package com.chat.cache.message;

public class MessageReaderFactory {

	public StringMessageReader createReader(AllocateBuffer allocateBuffer) {
		return new StringMessageReader();
	}
}
