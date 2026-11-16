package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

import java.time.LocalTime;
import java.util.Optional;

public class NotificationCommand implements BotCommand{

    private static final String EXAMPLE_NOTIFICATION_COMMAND = "<code>" +
            Command.NOTIFICATION.getText() + " 15 30</code>";
    private static final String NOTIFICATION_TEXT = "Выберете время нотификации, либо введите команду," +
            "например " + EXAMPLE_NOTIFICATION_COMMAND + " (Разделитель часов и минут - пробел)";
    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат команды." + System.lineSeparator() +
            "Для того, чтобы указать время нотификации, нажмите кнопку ниже, " +
            "или введите команду в формате: " + EXAMPLE_NOTIFICATION_COMMAND;

    private static final String[] BUTTON_NAMES = {
            "12:00",
            "15:00",
            "17:00",
            "19:00",
            "21:00",
    };
    private static final String[] BUTTON_COMMANDS = {
            Command.NOTIFICATION.getText() + " 12 00",
            Command.NOTIFICATION.getText() + " 15 00",
            Command.NOTIFICATION.getText() + " 17 00",
            Command.NOTIFICATION.getText() + " 19 00",
            Command.NOTIFICATION.getText() + " 21 00"
    };

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.NOTIFICATION.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ", 3);
        if (commandParts.length <= 1 && commandParts[0].equalsIgnoreCase(Command.NOTIFICATION.getText())) {
            return MessageUtils.getConstructMessage(chatId, NOTIFICATION_TEXT,
                    KeyboardUtils.getKeyboard(BUTTON_COMMANDS, BUTTON_NAMES));
        } else if (commandParts.length == 3 && commandParts[0].equalsIgnoreCase(Command.NOTIFICATION.getText())) {
            int hours = Integer.parseInt(commandParts[1]);
            int minutes = Integer.parseInt(commandParts[2]);
            LocalTime notificationTime = LocalTime.of(hours,minutes);
            return saveNotificationSetting(chatId, notificationTime, userRepository);
        } else {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Время нотификации", Command.NOTIFICATION.getText()));
        }
    }

    private BotApiMethod<?> saveNotificationSetting(long chatId, LocalTime notificationTime,
                                                    UserRepository userRepository) {
        Optional<User> user = userRepository.findById(chatId);
        if (user.isPresent()) {
            User currentUser = user.get();
            UserSettings copyUserSetting =  currentUser.getUserSettings();
            copyUserSetting.setNotificationTime(notificationTime);
            currentUser.setUserSettings(copyUserSetting);
            userRepository.update(currentUser);
            return MessageUtils.getConstructMessage(chatId, SUCCESS_CHANGE_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
        } else {
            return MessageUtils.getUnauthorizedMessage(chatId);
        }
    }
}
