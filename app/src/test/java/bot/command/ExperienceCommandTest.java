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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperienceCommandTest extends AbstractCommandTest{

    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String EXAMPLE_EXPERIENCE_COMMAND = "<code>" + Command.EXPERIENCE.getText() + " 5</code>";
    private static final String EXPERIENCE_TEXT = "Выберете минимальный опыт, или введите команду в формате: " +
            EXAMPLE_EXPERIENCE_COMMAND;

    private BotCommand experienceCommand;
    private final String message = "/experience 5";

    @BeforeEach
    void setUp() {
        experienceCommand = new ExperienceCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/experience 5",
            "/experience 0",
            "/experience -20"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(experienceCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(experienceCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(SUCCESS_CHANGE_TEXT, actualSendMessage.getText());
        verify(userRepository, times(1)).update(user);
        assertEquals(5, user.getUserSettings().getExperience());
    }

    @Test
    void givenUser_whenExecuteCommand_UnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(experienceCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @Test
    void givenExperienceCommand_whenExecuteCommand_thenExperienceTextMessage() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(experienceCommand, "/experience");
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(EXPERIENCE_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }
}
