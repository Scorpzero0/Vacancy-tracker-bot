package util;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public final class MessageUtils {

    private static final String UNAUTHORIZED_USER_TEXT = "Пользователь не найден! " +
            "Пожалуйста авторизируйтесь с помощью команды /start";

    private MessageUtils() {
        /* This utility class should not be instantiated */
    }

    public static SendMessage getConstructMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage.SendMessageBuilder messageBuilder = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML");
        if (keyboard != null) {
            messageBuilder.replyMarkup(keyboard);
        }
        return messageBuilder.build();
    }

    public static BotApiMethod<?> getUnauthorizedMessage(long chatId) {
        return getConstructMessage(chatId, UNAUTHORIZED_USER_TEXT,
                KeyboardUtils.getOneButtonKeyboard("Старт", "/start"));
    }
}
