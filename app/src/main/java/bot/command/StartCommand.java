package bot.command;

import model.MessageData;
import model.User;
import model.UserSettings;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;
import java.util.Optional;

public class StartCommand implements BotCommand{

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.START.getText());
    }

    @Override
    public SendMessage executeCommand(MessageData messageData, UserRepository userRepository) {
        long userId = messageData.getUserId();
        long chatId = messageData.getChatId();
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            String text = getGreetingsText(user.get());
            return MessageUtils.getConstructMessage(chatId, text,
                    KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
        } else {
            userRepository.save(new User(userId, chatId, new UserSettings()));
            String text = getRegisterText();
            return MessageUtils.getConstructMessage(chatId, text,
                    KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
        }
    }

    private String getGreetingsText (User user) {
        return "Vacancy Tracker Bot приветствует вас!" + System.lineSeparator()
                + "Вы ранее регистрировались в нашем боте, можете приступать к его использованию."
                + System.lineSeparator()
                + "Сейчас используется часовой пояс UTC +3:00, для изменения введите, например <code>/UTC +1:00</code>"
                + System.lineSeparator() + System.lineSeparator()
                + "Ранее заданные фильтры: " + System.lineSeparator()
                + user.getUserSettings().toString() + System.lineSeparator() + System.lineSeparator()
                + "Введите команду /menu для получения списка команд";
    }

    private String getRegisterText() {
        return "Вы успешно зарегистрированы в Vacancy Tracker Bot!" + System.lineSeparator()
                + "Будет использован часовой пояс UTC, " +
                "для изменения введите часовой пояс в формате <code>/UTC +3</code>"
                + System.lineSeparator()
                + "Используйте команду /menu для получения списка команд";
    }
}
