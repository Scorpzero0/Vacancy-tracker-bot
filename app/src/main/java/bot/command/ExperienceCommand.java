package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;
import java.util.Optional;

public class ExperienceCommand implements BotCommand {

    private static final String EXAMPLE_EXPERIENCE_COMMAND = "<code>" + Command.EXPERIENCE.getText() + " 5</code>";
    private static final String EXPERIENCE_TEXT = "Выберете минимальный опыт, или введите команду в формате: " +
            EXAMPLE_EXPERIENCE_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат команды." + System.lineSeparator() +
            "Для того, чтобы указать опыт, нажмите кнопку ниже, " +
            "или введите команду в формате: " + EXAMPLE_EXPERIENCE_COMMAND;


    private static final String[] BUTTON_NAMES = {
            "Не учитывать",
            "0 Без опыта работы",
            "1 От года",
            "3 От трёх лет",
            "5 От пяти лет",
    };
    private static final String[] BUTTON_COMMANDS = {
            Command.EXPERIENCE.getText() + " -1",
            Command.EXPERIENCE.getText() + " 0",
            Command.EXPERIENCE.getText() + " 1",
            Command.EXPERIENCE.getText() + " 3",
            Command.EXPERIENCE.getText() + " 5"
    };

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.EXPERIENCE.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ");
        if (commandParts.length <= 1 && commandParts[0].equals(Command.EXPERIENCE.getText())) {
            return MessageUtils.getConstructMessage(chatId, EXPERIENCE_TEXT,
                    KeyboardUtils.getKeyboard(BUTTON_COMMANDS, BUTTON_NAMES));
        } else if (commandParts.length == 2 && commandParts[0].equals(Command.EXPERIENCE.getText())) {
            int experience = Integer.parseInt(commandParts[1]);
            return saveExperienceSetting(chatId, experience, userRepository);
        } else {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Минимальный опыт", Command.EXPERIENCE.getText()));
        }
    }

    private BotApiMethod<?> saveExperienceSetting(long chatId, int experience, UserRepository userRepository) {
        Optional<User> user = userRepository.findById(chatId);
        if (user.isPresent()) {
            User currentUser = user.get();
            UserSettings copyUserSetting =  currentUser.getUserSettings();
            copyUserSetting.setExperience(experience);
            currentUser.setUserSettings(copyUserSetting);
            userRepository.update(currentUser);
            return MessageUtils.getConstructMessage(chatId, SUCCESS_CHANGE_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
        } else {
            return MessageUtils.getUnauthorizedMessage(chatId);
        }
    }
}
