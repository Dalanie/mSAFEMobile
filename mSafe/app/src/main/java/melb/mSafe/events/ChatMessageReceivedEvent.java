package melb.mSafe.events;

import melb.mSafe.chat.TextMessage;

/**
 * Created by Daniel on 26.12.13.
 */
public class ChatMessageReceivedEvent {

    public TextMessage message;

    public ChatMessageReceivedEvent(TextMessage message){
        this.message = message;
    }
}
