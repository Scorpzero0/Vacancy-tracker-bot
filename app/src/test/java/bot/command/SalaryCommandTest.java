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

class SalaryCommandTest extends AbstractCommandTest{

    private static final String EXAMPLE_SALARY_COMMAND = "<code>" + Command.SALARY.getText() + " 100000</code>";
    private static final String SALARY_TEXT = "Введите желаемую минимальную зарплату из предложенных вариантов," +
            "или введите команду, например " + EXAMPLE_SALARY_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";

    private SalaryCommand salaryCommand;
    private final String message = "/salary 110000";

    @BeforeEach
    void setUp() {
        salaryCommand = new SalaryCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/salary java developer",
            "/salary",
            "/salary 110000"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(salaryCommand.isApply(messageData));
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(salaryCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(SUCCESS_CHANGE_TEXT, actualSendMessage.getText());
        verify(userRepository, times(1)).update(user);
        assertEquals(110000, user.getUserSettings().getSalary());
    }

    @Test
    void givenUser_whenExecuteCommand_thenUnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(salaryCommand, message);
        assertEquals(actualSendMessage.getChatId(), String.valueOf(chatId));
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @Test
    void givenSalaryCommand_whenExecuteCommand_thenExperienceText() {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(salaryCommand, "/salary");
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(SALARY_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }
}
