package bot.command;

import bot.scheduler.BotSchedulerManager;
import exception.InvalidCommandFormat;
import model.MessageData;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

public class StopCommand implements BotCommand{

    private static final String GRATEFUL_TEXT = "Благодарим, что продолжаете пользоваться Vacancy Tracker Bot";

    private final BotSchedulerManager botSchedulerManager;

    public StopCommand(BotSchedulerManager botSchedulerManager) {
        this.botSchedulerManager = botSchedulerManager;
    }

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.STOP.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String[] commandParts = messageData.getMessage().split(" ");
        if (!userRepository.exists(chatId)) {
            return MessageUtils.getUnauthorizedMessage(chatId);
        }
        if (commandParts.length == 1 && commandParts[0].equalsIgnoreCase(Command.STOP.getText())) {
            return MessageUtils.getConstructMessage(chatId, getCheckDeleteText(),
                    KeyboardUtils.getCheckDeleteKeyboard());
        } else if (commandParts.length == 2 && commandParts[0].equalsIgnoreCase(Command.STOP.getText())) {
            return deleteArgsExecute(commandParts[1], userRepository, chatId);
        } else {
            throw new InvalidCommandFormat("Incorrect parameter in command /stop");
        }
    }

    private BotApiMethod<?> deleteArgsExecute(String commandPart, UserRepository userRepository, long chatId) {
        switch (commandPart) {
            case "yes" -> {
                botSchedulerManager.cancelDailyMessage(String.valueOf(chatId));
                userRepository.delete(chatId);
                return MessageUtils.getConstructMessage(chatId, getDeleteText(),
                        KeyboardUtils.getOneButtonKeyboard("Старт", "/start"));
            }
            case "no" -> {
                return MessageUtils.getConstructMessage(chatId, GRATEFUL_TEXT,
                        KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
            }
            default -> throw new InvalidCommandFormat("Incorrect parameter in command /stop");
        }
    }

    private String getDeleteText() {
        return "Вся информация, связанная с вашими данными, удалена!" + System.lineSeparator()
                + "Благодарим за использование Vacancy Tracker Bot!";
    }

    private String getCheckDeleteText() {
        return "Вся информация, связанная с вашими данными, будет удалена! "
                + System.lineSeparator() +
                "Вы уверены?";
    }
}
