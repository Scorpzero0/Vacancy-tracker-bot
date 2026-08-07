package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import repository.UserRepository;
import repository.ApiResponseRepository;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractCommandTest {

    protected static final String UNAUTHORIZED_USER_TEXT = "Пользователь не найден! " +
            "Пожалуйста авторизируйтесь с помощью команды /start";

    @Mock
    protected UserRepository userRepository;
    @Mock
    protected ApiResponseRepository apiResponseRepository;
    protected final long chatId = 12345L;
    protected final long userId = 12345L;
    protected final int messageId = 1;
    protected final String callbackQueryId = "1";

    protected User createTestUserWithoutUserSettings() {
        User user = new User();
        user.setUserSettings(new UserSettings());
        return user;
    }

    protected User createTestUserWithUserSettings() {
        UserSettings userSettings = new UserSettings();
        userSettings.setTimeZone("+03:00");
        userSettings.setRegion(50);
        userSettings.setExperience(5);
        userSettings.setSalary(50000L);
        userSettings.setKeyword("Test");
        userSettings.setNotificationTime(LocalTime.of(12, 30));
        return new User(userId, chatId, userSettings);
    }

    protected BotApiMethod<?> getActualSendMessage(BotCommand command, String message) {
        MessageData messageData = new MessageData(userId, chatId, messageId, message, callbackQueryId);
        BotApiMethod<?> actualResult = command.executeCommand(messageData, userRepository);
        assertInstanceOf(SendMessage.class, actualResult);
        return actualResult;
    }

    protected MessageData getActualMessageData(String message) {
        return new MessageData(userId, chatId, messageId, message, callbackQueryId);
    }
}
