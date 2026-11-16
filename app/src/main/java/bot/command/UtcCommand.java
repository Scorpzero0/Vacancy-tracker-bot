package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.Optional;

public class UtcCommand implements BotCommand{

    private static final String EXAMPLE_UTC_COMMAND = "<code>" + Command.UTC.getText() + " +2</code>";
    private static final String TIMEZONE_TEXT = "Введите вашу временную зону в формате " + EXAMPLE_UTC_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Временная зона успешно изменена";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат часового пояса. " + System.lineSeparator() +
            "Для того, чтобы указать часовой пояс, введите команду в формате: " + EXAMPLE_UTC_COMMAND;

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.UTC.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ");
        if (commandParts.length == 1 && commandParts[0].equalsIgnoreCase(Command.UTC.getText())) {
            return MessageUtils.getConstructMessage(chatId, TIMEZONE_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", Command.MENU.getText()));
        } else if (commandParts.length == 2 && commandParts[0].equalsIgnoreCase(Command.UTC.getText())) {
            String rawInput = commandParts[1].trim();
            try {
                String validatedZone = parseAndValidateUtc(rawInput);
                return saveTimeZoneSettings(chatId, validatedZone, userRepository);
            } catch (IllegalArgumentException | DateTimeException e) {
                return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                        KeyboardUtils.getOneButtonKeyboard("Меню", Command.MENU.getText()));
            }
        } else {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", Command.MENU.getText()));
        }
    }

    private String parseAndValidateUtc(String input) {
        if (!input.startsWith("+") && !input.startsWith("-") && !input.equals("0")) {
            input = "+" + input;
        }
        ZoneOffset offset = ZoneOffset.of(input);
        return offset.getId();
    }

    private BotApiMethod<?> saveTimeZoneSettings(long chatId, String timeZone, UserRepository userRepository) {
        Optional<User> user = userRepository.findById(chatId);
        if (user.isEmpty()) {
            return MessageUtils.getUnauthorizedMessage(chatId);
        } else {
            User currentUser = user.get();
            UserSettings settings = currentUser.getUserSettings();
            settings.setTimeZone(timeZone);
            currentUser.setUserSettings(settings);
            userRepository.update(currentUser);
            return MessageUtils.getConstructMessage(chatId, SUCCESS_CHANGE_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", Command.MENU.getText()));
        }
    }
}
