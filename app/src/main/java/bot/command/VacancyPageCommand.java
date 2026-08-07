package bot.command;

import bot.scheduler.BotSchedulerManager;
import model.MessageData;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;

public class VacancyPageCommand implements BotCommand{

    private final BotSchedulerManager botSchedulerManager;

    public VacancyPageCommand(BotSchedulerManager botSchedulerManager) {
        this.botSchedulerManager = botSchedulerManager;
    }

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage() != null && messageData.getMessage().startsWith("vac_page_");
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        String callbackData = messageData.getMessage();
        String chatId = String.valueOf(messageData.getChatId());
        String callbackQueryId = messageData.getCallbackQueryId();
        int targetPage = Integer.parseInt(callbackData.replace("vac_page_", ""));
        botSchedulerManager.sendVacanciesPage(chatId, targetPage);
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
    }
}
