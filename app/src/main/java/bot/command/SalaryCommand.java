package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;
import java.util.Optional;

public class SalaryCommand implements BotCommand{

    private static final String EXAMPLE_SALARY_COMMAND = "<code>" + Command.SALARY.getText() + " 100000</code>";
    private static final String SALARY_TEXT = "Введите желаемую минимальную зарплату из предложенных вариантов," +
            "или введите команду, например " + EXAMPLE_SALARY_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат команды."
            + System.lineSeparator()
            + "Для того, чтобы указать зарплату, нажмите кнопку ниже, " +
            "или введите команду в формате: " + EXAMPLE_SALARY_COMMAND;

    private static final String[] BUTTON_NAMES = {
            "Не учитывать",
            "50000 От 50 тыс. руб",
            "80000 От 80 тыс. руб",
            "100000 От 100 тыс. руб",
            "120000 От 120 тыс. руб",
    };
    private static final String[] BUTTON_COMMANDS = {
            Command.SALARY.getText() + " 0",
            Command.SALARY.getText() + " 50000",
            Command.SALARY.getText() + " 80000",
            Command.SALARY.getText() + " 100000",
            Command.SALARY.getText() + " 120000"
    };

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.SALARY.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ", 2);
        if (commandParts.length <= 1 && commandParts[0].equalsIgnoreCase(Command.SALARY.getText())) {
            return MessageUtils.getConstructMessage(chatId, SALARY_TEXT,
                    KeyboardUtils.getKeyboard(BUTTON_COMMANDS, BUTTON_NAMES));
        } else if (commandParts.length == 2 && commandParts[0].equalsIgnoreCase(Command.SALARY.getText())){
            int salary = Integer.parseInt(commandParts[1]);
            return saveSalarySetting(chatId, salary, userRepository);
        } else {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Зарплата", Command.SALARY.getText()));
        }
    }

    private BotApiMethod<?> saveSalarySetting(long chatId, int salary, UserRepository userRepository) {
        Optional<User> user = userRepository.findById(chatId);
        if (user.isPresent()) {
            User currentUser = user.get();
            UserSettings copyUserSetting =  currentUser.getUserSettings();
            copyUserSetting.setSalary((long) salary);
            currentUser.setUserSettings(copyUserSetting);
            userRepository.update(currentUser);
            return MessageUtils.getConstructMessage(chatId, SUCCESS_CHANGE_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
        } else {
            return MessageUtils.getUnauthorizedMessage(chatId);
        }
    }
}
