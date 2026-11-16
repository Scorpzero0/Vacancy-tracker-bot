package model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MessageData {
    private long userId;
    private long chatId;
    private int messageId;
    private String message;
    private String callbackQueryId;

    public MessageData(long userId, long chatId, int messageId, String message, String callbackQueryId) {
        this.userId = userId;
        this.chatId = chatId;
        this.messageId = messageId;
        this.message = message;
        this.callbackQueryId = callbackQueryId;
    }
}
