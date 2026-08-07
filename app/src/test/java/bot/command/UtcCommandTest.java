package bot.command;

import model.MessageData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

class UtcCommandTest extends AbstractCommandTest {

    private static final String EXAMPLE_UTC_COMMAND = "<code>" + Command.UTC.getText() + " +2</code>";
    private static final String TIMEZONE_TEXT = "Введите вашу временную зону в формате " + EXAMPLE_UTC_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Временная зона успешно изменена";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат часового пояса. " + System.lineSeparator() +
            "Для того, чтобы указать часовой пояс, введите команду в формате: " + EXAMPLE_UTC_COMMAND;

    private UtcCommand utcCommand;
    private final String message = "/UTC +3";

    @BeforeEach
    void setUp() {
        utcCommand = new UtcCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/UTC -1",
            "/UTC",
            "/UTC test 22 67"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(utcCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(utcCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(SUCCESS_CHANGE_TEXT, actualSendMessage.getText());
        verify(userRepository, times(1)).update(user);
        assertEquals("+03:00", user.getUserSettings().getTimeZone());
    }

    @Test
    void givenUser_whenExecuteCommand_thenUnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(utcCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @Test
    void givenExperienceCommand_whenExecuteCommand_thenUtcTextMessage() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(utcCommand, "/UTC");
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(TIMEZONE_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @ParameterizedTest
    @CsvSource({
            "/UTC 20 test 80",
            "/UTC test",
    })
    void givenInvalidCommandParams_whenExecuteCommand_thenErrorUtcMessage(String message) {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(utcCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(ERROR_COMMAND_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }
}
