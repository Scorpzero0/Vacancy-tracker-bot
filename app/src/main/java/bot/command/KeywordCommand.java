package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

import java.util.Optional;

public class KeywordCommand implements BotCommand{

    private static final String EXAMPLE_KEYWORD_COMMAND = "<code>" + Command.KEYWORD.getText() +
            " слова для поиска</code>";
    private static final String KEYWORD_TEXT = "Выберете ключевое слово из предложенных вариантов или " +
            "введите команду, например " + EXAMPLE_KEYWORD_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Ключевые слова были успешно сохранены";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат команды." + System.lineSeparator() +
            "Для того, чтобы указать ключевые слова, нажмите кнопку ниже, " +
            "или введите команду в формате " + EXAMPLE_KEYWORD_COMMAND;

    private static final String[] BUTTON_NAMES = {
            "Программист",
            "Developer",
            "Аналитик",
            "Тестировщик",
            "Стажировка"
    };
    private static final String[] BUTTON_COMMANDS = {
            Command.KEYWORD.getText() + " Программист",
            Command.KEYWORD.getText() + " Developer",
            Command.KEYWORD.getText() + " Аналитик",
            Command.KEYWORD.getText() + " Тестировщик",
            Command.KEYWORD.getText() + " Стажировка",
    };

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.KEYWORD.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ", 2);
        if (commandParts.length <= 1 && commandParts[0].equalsIgnoreCase(Command.KEYWORD.getText())) {
            return MessageUtils.getConstructMessage(chatId, KEYWORD_TEXT,
                    KeyboardUtils.getKeyboard(BUTTON_COMMANDS, BUTTON_NAMES));
        } else if (commandParts.length == 2 && commandParts[0].equalsIgnoreCase(Command.KEYWORD.getText())) {
            String keyword = commandParts[1];
            return saveKeywordSetting(chatId, keyword, userRepository);
        } else {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Слова для поиска", Command.KEYWORD.getText()));
        }
    }

    private BotApiMethod<?> saveKeywordSetting(long chatId, String keyword, UserRepository userRepository) {
        Optional<User> user = userRepository.findById(chatId);
        if (user.isPresent()) {
            User currentUser = user.get();
            UserSettings newUserSettings = currentUser.getUserSettings();
            newUserSettings.setKeyword(keyword);
            currentUser.setUserSettings(newUserSettings);
            userRepository.update(currentUser);
            return MessageUtils.getConstructMessage(chatId, SUCCESS_CHANGE_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
        } else {
            return MessageUtils.getUnauthorizedMessage(chatId);
        }
    }
}
