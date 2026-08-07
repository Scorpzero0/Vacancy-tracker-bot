package bot.command;

import model.MessageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import repository.UserRepository;
import service.InputTransformService;

import java.util.List;

public class BotCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BotCommandExecutor.class);
    private final List<BotCommand> commands;
    InputTransformService inputTransformService;

    public BotCommandExecutor(List<BotCommand> commands, InputTransformService inputTransformService) {
        this.commands = commands;
        this.inputTransformService = inputTransformService;
    }

    public BotApiMethod<?> commandHandle(Update update, UserRepository userRepository) {
        MessageData messageData = inputTransformService.inputTransform(update);
        try {
            return commands.stream()
                    .filter(botCommand -> botCommand.isApply(messageData))
                    .findFirst()
                    .orElse(new UnknowCommand())
                    .executeCommand(messageData, userRepository);
        } catch (NumberFormatException e) {
            logger.error("Неверный формат команды");
        }
        UnknowCommand invalidCommand = new UnknowCommand();
        return invalidCommand.executeCommand(messageData, userRepository);
    }
}
