package bot.command;

import model.MessageData;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

public class UnknowCommand implements BotCommand {

    @Override
    public boolean isApply(MessageData messageData) {
        return false;
    }

    @Override
    public SendMessage executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        String text = "Неверно введена команда! Проверьте корректность написанной команды, " +
                "или воспользуйтесь кнопками ниже";
        return MessageUtils.getConstructMessage(chatId, text,
                KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
    }
}
