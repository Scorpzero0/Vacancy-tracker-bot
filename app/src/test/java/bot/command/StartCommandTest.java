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

class StartCommandTest extends AbstractCommandTest {

    private StartCommand startCommand;
    private final String message = "/start";

    @BeforeEach
    void setUp() {
        startCommand = new StartCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/start test",
            "/start",
            "/start test 22 67"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(startCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(startCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(getGreetingsText(user), actualSendMessage.getText());
    }

    @Test
    void givenUser_whenExecuteCommand_thenUnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(startCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(getRegisterText(), actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }


    private String getGreetingsText (User user) {
        return "Vacancy Tracker Bot приветствует вас!" + System.lineSeparator()
                + "Вы ранее регистрировались в нашем боте, можете приступать к его использованию."
                + System.lineSeparator()
                + "Сейчас используется часовой пояс UTC +3:00, для изменения введите, например <code>/UTC +1:00</code>"
                + System.lineSeparator() + System.lineSeparator()
                + "Ранее заданные фильтры: " + System.lineSeparator()
                + user.getUserSettings().toString() + System.lineSeparator() + System.lineSeparator()
                + "Введите команду /menu для получения списка команд";
    }

    private String getRegisterText() {
        return "Вы успешно зарегистрированы в Vacancy Tracker Bot!" + System.lineSeparator()
                + "Будет использован часовой пояс UTC, " +
                "для изменения введите часовой пояс в формате <code>/UTC +3</code>"
                + System.lineSeparator()
                + "Используйте команду /menu для получения списка команд";
    }
}
