package org.newdawn.commet.chat;

import java.io.IOException;

import org.newdawn.commet.message.Message;
import org.newdawn.commet.message.MessageFactory;

public class ChatMessageFactory implements MessageFactory {

	public Message createMessageFor(short id) throws IOException {
		if (id == ChatMessage.ID) {
			return new ChatMessage();
		}
		
		throw new IOException("Unknown message recieved: "+id);
	}

}
