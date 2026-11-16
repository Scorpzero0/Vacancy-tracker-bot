package bot.command;

import model.MessageData;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;

public interface BotCommand {

    boolean isApply(MessageData messageData);
    BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository);
}

