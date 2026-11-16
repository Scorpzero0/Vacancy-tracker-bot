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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuCommandTest extends AbstractCommandTest {

    private MenuCommand menuCommand;

    @BeforeEach
    void setUp() {
        menuCommand = new MenuCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/menu",
            "/menu 67",
            "/menu test",
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(menuCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(menuCommand, "/menu");
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(getMenuText(), actualSendMessage.getText());
    }

    @Test
    void givenUser_whenExecuteCommand_UnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(menuCommand, "/menu");
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
    }

    private String getMenuText() {
        return "Задайте критерии поиска и время нотификации." + System.lineSeparator()
                + "Затем нажмите \"Готово\", либо введите команду /ready";
    }
}
