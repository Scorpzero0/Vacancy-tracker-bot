package service;

import model.MessageData;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

public class InputTransformService {

    public MessageData inputTransform(Update update) {
        MessageData messageData = new MessageData();
        if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            messageData.setMessage(callback.getData());
            messageData.setChatId(callback.getMessage().getChatId());
            messageData.setUserId(callback.getMessage().getChatId());
            messageData.setMessageId(callback.getMessage().getMessageId());
            messageData.setCallbackQueryId(callback.getId());
        } else {
            messageData.setMessage(update.getMessage().getText());
            messageData.setChatId(update.getMessage().getChatId());
            messageData.setUserId(update.getMessage().getFrom().getId());
            messageData.setMessageId(update.getMessage().getMessageId());
        }
        return messageData;
    }
}
