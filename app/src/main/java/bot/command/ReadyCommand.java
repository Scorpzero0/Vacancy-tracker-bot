package bot.command;

import bot.scheduler.BotSchedulerManager;
import model.MessageData;
import model.User;
import model.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import repository.UserRepository;
import util.KeyboardUtils;
import util.MessageUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

public class ReadyCommand implements BotCommand{

    private final BotSchedulerManager schedulerManager;
    private static final Logger logger = LoggerFactory.getLogger(ReadyCommand.class);

    public ReadyCommand(BotSchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    @Override
    public boolean isApply(MessageData messageData) {
        return messageData.getMessage().startsWith(Command.READY.getText());
    }

    @Override
    public BotApiMethod<?> executeCommand(MessageData messageData, UserRepository userRepository) {
        long chatId = messageData.getChatId();
        Optional<User> user = userRepository.findById(chatId);
        if (user.isEmpty()) {
            return MessageUtils.getUnauthorizedMessage(chatId);
        } else {
            User currentUser = user.get();
            if (currentUser.getUserSettings().getNotificationTime() == null) {
                return MessageUtils.getConstructMessage(chatId, "Необходимо задать время нотификации!",
                        KeyboardUtils.getOneButtonKeyboard("Настройка нотификации",
                                "/notify"));
            } else {
                startActiveTask(currentUser);
                return MessageUtils.getConstructMessage(chatId, getScheduleTaskText(currentUser),
                        KeyboardUtils.getOneButtonKeyboard("Меню", "/menu"));
            }
        }
    }

    private void startActiveTask(User user) {
        UserSettings settings = user.getUserSettings();
        LocalTime notificationTime = settings.getNotificationTime();
        String rawZone = settings.getTimeZone() != null ? settings.getTimeZone() : "GMT+3";
        try {
            int hour = notificationTime.getHour();
            int minute = notificationTime.getMinute();
            ZoneId userZoneId = ZoneId.of(rawZone);
            String chatIdStr = String.valueOf(user.getChatId());
            schedulerManager.scheduleDailyMessage(chatIdStr, hour, minute, userZoneId);
            logger.info("Планировщик успешно зарегистрировал задачу для чата: {}", chatIdStr);

        } catch (Exception e) {
            logger.error("Ошибка при старте отложенного задания для пользователя {}: {}",
                    user.getChatId(), e.getMessage());
        }
    }

    private String getScheduleTaskText(User user) {
        UserSettings userSettings = user.getUserSettings();
        return "Запланировано выполнение задачи нотификации на " + userSettings.getNotificationTime() + ". " +
                "Будут искаться новые вакансии по следующим параметрам: " +
                System.lineSeparator() + System.lineSeparator() + userSettings;
    }
}
