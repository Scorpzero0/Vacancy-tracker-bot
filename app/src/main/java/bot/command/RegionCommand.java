package bot.command;

import exception.InvalidCommandFormat;
import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

import java.util.Optional;

public class RegionCommand implements BotCommand {

    private static final String EXAMPLE_REGION_COMMAND = "<code>" + Command.REGION.getText() + " 50</code>";
    private static final String REGION_TEXT = "Выберете регион, или введите команду в формате: " +
            EXAMPLE_REGION_COMMAND;
    private static final String SUCCESS_CHANGE_TEXT = "Изменения были сохранены.";
    private static final String ERROR_COMMAND_TEXT = "Неверный формат команды."
            + System.lineSeparator() +
            "Для того, чтобы указать регион, нажмите кнопку ниже, или введите команду в формате: "
            + EXAMPLE_REGION_COMMAND;

    @Override
    public boolean isApply(MessageData messageData) {
        String msg = messageData.getMessage();
        if (msg == null) return false;
        return msg.startsWith(Command.REGION.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ", 3);
        if (commandParts.length <= 1) {
            return MessageUtils.getConstructMessage(chatId, REGION_TEXT, KeyboardUtils.getRegionKeyboard(0));
        } else if (commandParts.length == 2 && commandParts[0].equalsIgnoreCase(Command.REGION.getText())) {
            int regionCode = Integer.parseInt(commandParts[1]);
            return saveRegionSettings(chatId, regionCode, userRepository);
        } else if (commandParts.length == 3
                && commandParts[0].equalsIgnoreCase(Command.REGION.getText())
                && commandParts[1].equalsIgnoreCase("page")) {
            return MessageUtils.getConstructMessage(chatId, REGION_TEXT,
                    KeyboardUtils.getRegionKeyboard(Integer.parseInt(commandParts[2])));
        } else {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Регион", Command.REGION.getText()));
        }
    }

    private BotApiMethod<?> saveRegionSettings(long chatId, int regionCode, UserRepository userRepository) {
        try {
            if (regionCode < 0 || regionCode > 96) {
                throw new InvalidCommandFormat("Incorrect region code");
            } else {
                Optional<User> user = userRepository.findById(chatId);
                if (user.isPresent()) {
                    User currentUser = user.get();
                    UserSettings copySettings = currentUser.getUserSettings();
                    copySettings.setRegion(regionCode);
                    currentUser.setUserSettings(copySettings);
                    userRepository.update(currentUser);
                    return MessageUtils.getConstructMessage(chatId, SUCCESS_CHANGE_TEXT,
                            KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
                } else {
                    return MessageUtils.getUnauthorizedMessage(chatId);
                }
            }
        } catch (InvalidCommandFormat e) {
            return MessageUtils.getConstructMessage(chatId, ERROR_COMMAND_TEXT,
                    KeyboardUtils.getOneButtonKeyboard("Регион", Command.REGION.getText()));
        }
    }
}
