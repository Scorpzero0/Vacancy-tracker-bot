package bot.command;

import model.MessageData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationCommandTest extends AbstractCommandTest{

    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String EXAMPLE_NOTIFICATION_COMMAND = "<code>" +
            Command.NOTIFICATION.getText() + " 15 30</code>";
    private static final String NOTIFICATION_TEXT = "Выберете время нотификации, либо введите команду," +
            "например " + EXAMPLE_NOTIFICATION_COMMAND + " (Разделитель часов и минут - пробел)";

    private NotificationCommand notificationCommand;
    private final String message = "/notify 12 20";

    @BeforeEach
    void setUp() {
        notificationCommand = new NotificationCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/notify",
            "/notify 12 40",
            "/notify test 24 00"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(notificationCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(notificationCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(SUCCESS_CHANGE_TEXT, actualSendMessage.getText());
        verify(userRepository, times(1)).update(user);
        assertEquals(LocalTime.of(12,20), user.getUserSettings().getNotificationTime());
    }

    @Test
    void givenUser_whenExecuteCommand_UnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(notificationCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @Test
    void givenNotificationCommand_whenExecuteCommand_thenNotificationText() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(notificationCommand, "/notify");
        assertEquals(NOTIFICATION_TEXT, actualSendMessage.getText());
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        verify(userRepository, never()).update(any());
    }
}
