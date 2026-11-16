package bot.command;

import bot.scheduler.BotSchedulerManager;
import exception.InvalidCommandFormat;
import model.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StopCommandTest extends AbstractCommandTest {

    private static final String GRATEFUL_TEXT = "Благодарим, что продолжаете пользоваться Vacancy Tracker Bot";

    private StopCommand stopCommand;
    @Mock
    private BotSchedulerManager botSchedulerManager;

    @BeforeEach
    void setUp() {
        stopCommand = new StopCommand(botSchedulerManager);
        lenient().when(userRepository.exists(anyLong())).thenReturn(true);
    }

    @ParameterizedTest
    @CsvSource({
            "/stop yes",
            "/stop",
            "/stop no",
            "/stop test 70 29"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(stopCommand.isApply(messageData));
    }

    @ParameterizedTest
    @CsvSource({
            "/stoptest",
            "/stop 289",
            "/stop test 578"
    })
    void givenInvalidCommandParams_whenExecuteCommand_thenThrow(String message) {
        MessageData messageData = new MessageData(userId, chatId, messageId, message, "1");
        assertThrows(InvalidCommandFormat.class, ()-> stopCommand.executeCommand(messageData, userRepository));
    }

    @Test
    void givenYesDeleteCommand_whenExecuteCommand_thenDeleteText() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(stopCommand, "/stop yes");
        assertEquals(getDeleteText(), actualSendMessage.getText());
        verify(userRepository, times(1)).delete(anyLong());
        verify(botSchedulerManager, times(1)).cancelDailyMessage(anyString());
    }

    @Test
    void givenNoDeleteCommand_whenExecuteCommand_thenDeleteText() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(stopCommand, "/stop no");
        assertEquals(GRATEFUL_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void givenDeleteCommand_whenExecuteCommand_thenDeleteText() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(stopCommand, "/stop");
        assertEquals(getCheckDeleteText(), actualSendMessage.getText());
        verify(userRepository, never()).delete(any());
    }

    private String getDeleteText() {
        return "Вся информация, связанная с вашими данными, удалена!" + System.lineSeparator()
                + "Благодарим за использование Vacancy Tracker Bot!";
    }

    private String getCheckDeleteText() {
        return "Вся информация, связанная с вашими данными, будет удалена! "
                + System.lineSeparator() +
                "Вы уверены?";
    }
}
