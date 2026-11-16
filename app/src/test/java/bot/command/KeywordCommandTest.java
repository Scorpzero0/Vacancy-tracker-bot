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
class KeywordCommandTest extends AbstractCommandTest{

    private static final String SUCCESS_CHANGE_TEXT = "Ключевые слова были успешно сохранены";
    private static final String EXAMPLE_KEYWORD_COMMAND = "<code>" + Command.KEYWORD.getText() +
            " слова для поиска</code>";
    private static final String KEYWORD_TEXT = "Выберете ключевое слово из предложенных вариантов или " +
            "введите команду, например " + EXAMPLE_KEYWORD_COMMAND;

    private KeywordCommand keywordCommand;
    private final String message = "/keyword java";

    @BeforeEach
    void setUp() {
        keywordCommand = new KeywordCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/keyword java developer",
            "/keyword",
            "/keyword test 22 67"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(keywordCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(keywordCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(SUCCESS_CHANGE_TEXT, actualSendMessage.getText());
        verify(userRepository, times(1)).update(user);
        assertEquals("java", user.getUserSettings().getKeyword());
    }

    @Test
    void givenUser_whenExecuteCommand_thenUnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(keywordCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @Test
    void givenKeywordCommand_whenExecuteCommand_thenKeywordTextMessage() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(keywordCommand, "/keyword");
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(KEYWORD_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }
}
