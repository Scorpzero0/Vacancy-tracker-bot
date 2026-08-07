package bot.command;

import model.MessageData;
import model.User;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;
import java.util.Optional;

public class MenuCommand implements BotCommand{

    private static final String[] BUTTON_COMMANDS = {
            Command.REGION.getText(),
            Command.EXPERIENCE.getText(),
            Command.SALARY.getText(),
            Command.KEYWORD.getText(),
            Command.NOTIFICATION.getText(),
            Command.READY.getText()};

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.MENU.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        Optional<User> user = userRepository.findById(chatId);
        if (user.isEmpty()) {
            return MessageUtils.getUnauthorizedMessage(chatId);
        } else {
            String[] dynamicButtonNames = KeyboardUtils.getDynamicButtonNames(user.get());
            return MessageUtils.getConstructMessage(chatId, getMenuText(),
                    KeyboardUtils.getKeyboard(BUTTON_COMMANDS, dynamicButtonNames));
        }
    }

    private String getMenuText() {
        return "Задайте критерии поиска и время нотификации." + System.lineSeparator()
                + "Затем нажмите \"Готово\", либо введите команду /ready";
    }
}
