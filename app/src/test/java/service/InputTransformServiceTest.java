package service;

import model.MessageData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InputTransformServiceTest {

    @InjectMocks
    private InputTransformService inputTransformService;
    @Mock
    private Update update;
    @Mock
    private Message message;

    private final long chatId = 12345L;
    private final int messageId = 1;

    @Test
    void givenUpdateWithCallback_whenInputTransform_thenMessageData() {
        CallbackQuery callbackQuery = Mockito.mock(CallbackQuery.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getData()).thenReturn("callback_data_123");
        String callbackQueryId = "callback_123";
        when(callbackQuery.getId()).thenReturn(callbackQueryId);
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        MessageData actual = inputTransformService.inputTransform(update);
        assertEquals("callback_data_123", actual.getMessage());
        assertEquals(chatId, actual.getChatId());
        assertEquals(chatId, actual.getUserId());
        assertEquals(messageId, actual.getMessageId());
        assertEquals(callbackQueryId, actual.getCallbackQueryId());
    }

    @Test
    void givenUpdateWithoutCallback_whenInputTransform_thenMessageData() {
        User user = Mockito.mock(User.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn("/start");
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);
        when(message.getFrom()).thenReturn(user);
        long userId = 12345L;
        when(user.getId()).thenReturn(userId);
        MessageData actual = inputTransformService.inputTransform(update);
        assertEquals("/start", actual.getMessage());
        assertEquals(chatId, actual.getChatId());
        assertEquals(userId, actual.getUserId());
        assertEquals(messageId, actual.getMessageId());
        assertNull(actual.getCallbackQueryId());
    }
}
