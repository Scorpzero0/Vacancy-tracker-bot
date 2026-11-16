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

class RegionCommandTest extends AbstractCommandTest {

    private static final String EXAMPLE_REGION_COMMAND = "<code>" + Command.REGION.getText() + " 50</code>";
    private static final String REGION_TEXT = "Выберете регион, или введите команду в формате: " +
            EXAMPLE_REGION_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат команды."
            + System.lineSeparator() +
            "Для того, чтобы указать регион, нажмите кнопку ниже, или введите команду в формате: "
            + EXAMPLE_REGION_COMMAND;

    private RegionCommand regionCommand;
    private final String message = "/region 50";

    @BeforeEach
    void setUp() {
        regionCommand = new RegionCommand();
    }

    @ParameterizedTest
    @CsvSource({
            "/region",
            "/region 40",
            "/region test 24 00"
    })
    void givenMessageData_whenIsApply_thenTrue(String message) {
        MessageData messageData = getActualMessageData(message);
        assertTrue(regionCommand.isApply(messageData));
    }

    @ParameterizedTest
    @CsvSource({
            "/region 500",
            "/region -1",
            "/region 98",
            "/region 23 test",
            "/test 23"
    })
    void givenIncorrectRegionCode_ExecuteCommand_thenException(String message) {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(regionCommand, message);
        assertEquals(ERROR_COMMAND_TEXT, actualSendMessage.getText());
    }

    @Test
    void givenPresentUser_whenExecuteCommand_thenSuccessMessage() {
        User user = createTestUserWithoutUserSettings();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(regionCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(SUCCESS_CHANGE_TEXT, actualSendMessage.getText());
        verify(userRepository, times(1)).update(user);
        assertEquals(50, user.getUserSettings().getRegion());
    }

    @Test
    void givenUser_whenExecuteCommand_UnauthorizedMessage() {
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(regionCommand, message);
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        assertEquals(UNAUTHORIZED_USER_TEXT, actualSendMessage.getText());
        verify(userRepository, never()).update(any());
    }

    @ParameterizedTest
    @CsvSource({
            "/region",
            "/region page 2"
    })
    void givenCommandParams_whenExecuteCommand_thenRegionTextMessage(String message) {
        SendMessage actualSendMessage = (SendMessage) getActualSendMessage(regionCommand, message);
        assertEquals(REGION_TEXT, actualSendMessage.getText());
        assertEquals(String.valueOf(chatId), actualSendMessage.getChatId());
        verify(userRepository, never()).update(any());
    }
}
