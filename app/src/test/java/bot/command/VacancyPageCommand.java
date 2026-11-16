package bot.command;

import bot.scheduler.BotSchedulerManager;
import model.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyPageCommandTest extends AbstractCommandTest {
    private VacancyPageCommand vacancyPageCommand;

    @Mock
    private BotSchedulerManager botSchedulerManager;

    @BeforeEach
    void setUp() {
        vacancyPageCommand = new VacancyPageCommand(botSchedulerManager);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "vac_page_1",
            "vac_page_2",
            "vac_page_10",
            "vac_page_999"
    })
    void givenCorrectCallback_whenIsApply_thenTrue(String callbackMessage) {
        MessageData messageData = getActualMessageData(callbackMessage);
        assertTrue(vacancyPageCommand.isApply(messageData));
    }

    @ParameterizedTest
    @CsvSource({
            "vac_page",
            "page_1",
            "/start",
            "''",
    })
    void givenIncorrectCallback_whenIsApply_thenFalse(String callbackMessage) {
        MessageData messageData = getActualMessageData(callbackMessage);
        assertFalse(vacancyPageCommand.isApply(messageData));
    }

    @Test
    void givenNullMessage_whenIsApply_thenFalse() {
        MessageData messageData = getActualMessageData(null);
        assertFalse(vacancyPageCommand.isApply(messageData));
    }

    @Test
    void givenValidMessageData_whenExecuteCommand_thenCallManagerAndReturnAnswerCallback() {
        // Arrange
        String callbackData = "vac_page_5";
        MessageData messageData = getActualMessageData(callbackData);

        // Act
        BotApiMethod<?> result = vacancyPageCommand.executeCommand(messageData, userRepository);
        verify(botSchedulerManager, times(1))
                .sendVacanciesPage(String.valueOf(chatId), 5);
        assertInstanceOf(AnswerCallbackQuery.class, result);
        AnswerCallbackQuery answer = (AnswerCallbackQuery) result;
        assertEquals(callbackQueryId, answer.getCallbackQueryId());
    }

    @Test
    void givenInvalidPageFormat_whenExecuteCommand_thenThrowNumberFormatException() {
        String badCallbackData = "vac_page_abc";
        MessageData messageData = getActualMessageData(badCallbackData);
        assertThrows(NumberFormatException.class, () ->
                vacancyPageCommand.executeCommand(messageData, userRepository)
        );
        verifyNoInteractions(botSchedulerManager);
    }
}
