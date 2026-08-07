package bot.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import service.InputTransformService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotCommandExecutorTest extends AbstractCommandTest{

    private static final String EXAMPLE_KEYWORD_COMMAND = "<code>" + Command.KEYWORD.getText() +
            " слова для поиска</code>";
    private static final String KEYWORD_TEXT = "Выберете ключевое слово из предложенных вариантов или " +
            "введите команду, например " + EXAMPLE_KEYWORD_COMMAND;
    private static final String UNKNOW_COMMAND_TEXT = "Неверно введена команда! Проверьте корректность написанной команды, " +
            "или воспользуйтесь кнопками ниже";

    @Mock
    private InputTransformService inputTransformService;
    private BotCommandExecutor botCommandExecutor;
    private final Update update = new Update();

    @BeforeEach
    void setUp() {
        List<BotCommand> commands = List.of(new ExperienceCommand(), new MenuCommand(), new KeywordCommand());
        botCommandExecutor = new BotCommandExecutor(commands, inputTransformService);
    }

    @Test
    void givenUpdate_whenCommandHandle_thenCommandTextMessage() {
        when(inputTransformService.inputTransform(update)).thenReturn(getActualMessageData("/keyword"));
        BotApiMethod<?> actual = botCommandExecutor.commandHandle(update, userRepository);
        assertInstanceOf(SendMessage.class, actual);
        SendMessage actualMessage = (SendMessage) actual;
        assertEquals(KEYWORD_TEXT, actualMessage.getText());
    }

    @Test
    void givenInvalidCommand_whenCommandHandle_thenUnknowCommand() {
        when(inputTransformService.inputTransform(update)).thenReturn(getActualMessageData("/goMainMenu"));
        BotApiMethod<?> actual = botCommandExecutor.commandHandle(update, userRepository);
        assertInstanceOf(SendMessage.class, actual);
        SendMessage actualMessage = (SendMessage) actual;
        assertEquals(UNKNOW_COMMAND_TEXT, actualMessage.getText());
    }
}
