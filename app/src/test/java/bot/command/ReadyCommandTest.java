package bot.command;

import bot.scheduler.BotSchedulerManager;
import model.MessageData;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadyCommandTest extends AbstractCommandTest {

    private static final String NOTIFICATION_TEXT = "Необходимо задать время нотификации!";

    @Mock
    private BotSchedulerManager botSchedulerManager;
    private ReadyCommand readyCommand;

    @BeforeEach
    void setUp() {
        readyCommand = new ReadyCommand(botSchedulerManager);
    }

    @ParameterizedTest
    @CsvSource({
            "/ready test",
            "/ready",
            "/ready test 22 67"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(readyCommand.isApply(messageData));
    }

    @Test
    void givenUser_whenExecuteCommand_thenUnauthorizedMessage() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(readyCommand, "/ready");
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
    }

    @Test
    void givenPresentUserWithoutNotifyTime_whenExecuteCommand_thenScheduleTaskText() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(readyCommand, "/ready");
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(NOTIFICATION_TEXT, actualSendMessage.getText());
    }

    @Test
    void givenPresentUserWithNotifyTime_whenExecuteCommand_thenScheduleTaskText() {
        User user = createTestUserWithUserSettings();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(readyCommand, "/ready");
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(getScheduleTaskText(user), actualSendMessage.getText());
    }

    private String getScheduleTaskText(User user) {
        return "Запланировано выполнение задачи нотификации на "
                + user.getUserSettings().getNotificationTime() + ". " +
                "Будут искаться новые вакансии по следующим параметрам: " +
                System.lineSeparator() + System.lineSeparator() + user.getUserSettings();
    }
}
