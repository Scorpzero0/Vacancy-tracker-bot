package bot.scheduler;

import bot.command.Command;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import repository.UserRepository;
import repository.ApiResponseRepository;
import service.VacancyLoadTask;
import util.KeyboardUtils;
import java.io.File;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class BotSchedulerManager {

    private static final Logger logger = LoggerFactory.getLogger(BotSchedulerManager.class);
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> activeTasks = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final int CHAT_PAGE_SIZE = 10;

    private static final String PATH = "data/responses/";
    private static final String FILE_FORMAT = ".json";

    public BotSchedulerManager(TelegramClient telegramClient, UserRepository userRepository,
                               ApiResponseRepository apiResponseRepository) {
        this(telegramClient, Executors.newScheduledThreadPool(4), userRepository, apiResponseRepository);
    }

    BotSchedulerManager(TelegramClient telegramClient, ScheduledExecutorService scheduler,
                        UserRepository userRepository, ApiResponseRepository apiResponseRepository) {
        this.telegramClient = telegramClient;
        this.scheduler = scheduler;
        this.userRepository = userRepository;
        this.apiResponseRepository = apiResponseRepository;
    }

    public void scheduleDailyMessage(String chatId, int hour, int minute, ZoneId userZoneId) {
        cancelDailyMessage(chatId);
        ZonedDateTime now = ZonedDateTime.now(userZoneId);
        ZonedDateTime nextRun = now.with(LocalTime.of(hour, minute, 0));
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        long initialDelay = Duration.between(now, nextRun).getSeconds();
        long period = TimeUnit.DAYS.toSeconds(1);

        Runnable task = () -> {
            try {
                long userId = Long.parseLong(chatId);
                File jsonFile = new File(PATH + userId + FILE_FORMAT);
                if (jsonFile.exists()) {
                    jsonFile.delete();
                }
                if (apiResponseRepository.exists(userId)) {
                    apiResponseRepository.delete(userId);
                }
                VacancyLoadTask vacancyLoadTask = new VacancyLoadTask(httpClient, userRepository,
                        apiResponseRepository, userId);
                vacancyLoadTask.run();
                sendVacanciesPage(chatId, 1);
                logger.info("Вакансии по расписанию успешно инициализированы для чата: {}", chatId);
            } catch (Exception e) {
                logger.error("Ошибка обработки и отправки вакансий: {}", e.getMessage(), e);
            }
        };
        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
        activeTasks.put(chatId, scheduledTask);
        logger.info("Задача запланирована для {} на {}:{}", chatId, hour, minute);
    }

    public void sendVacanciesPage(String chatId, int page) {
        try {
            long userId = Long.parseLong(chatId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден с ID: " + userId));
            Optional<ApiResponse> apiResponseOpt = apiResponseRepository.getApiResponse(userId);
            if (apiResponseOpt.isEmpty() || apiResponseOpt.get().getResults() == null
                    || apiResponseOpt.get().getResults().getVacancies() == null) {
                sendMessageNotFoundVacancy(chatId);
                return;
            }
            List<VacancyWrapper> allVacancies = apiResponseOpt.get().getResults().getVacancies();
            if (allVacancies.isEmpty()) {
                sendMessageNotFoundVacancy(chatId);
                return;
            }
            int fromIndex = (page - 1) * CHAT_PAGE_SIZE;
            if (fromIndex >= allVacancies.size()) {
                UserSettings settings = user.getUserSettings();
                boolean isGlobalSearch = (settings == null) ||
                        ((settings.getKeyword() == null || settings.getKeyword().isEmpty())
                                && (settings.getRegion() == null || settings.getRegion() == 0));
                if (isGlobalSearch) {
                    sendAlertMessage(chatId);
                    return;
                }
                VacancyLoadTask vacancyLoadTask = new VacancyLoadTask(httpClient, userRepository, apiResponseRepository, userId);
                int apiOffset = (allVacancies.size() / 100) * 100;
                if (apiOffset == 0 && !allVacancies.isEmpty()) {
                    apiOffset = 100;
                }
                vacancyLoadTask.setOffset(apiOffset);
                vacancyLoadTask.run();
                allVacancies = apiResponseRepository.getApiResponse(userId).get().getResults().getVacancies();
            }
            if (fromIndex >= allVacancies.size()) {
                SendMessage endMessage = SendMessage.builder()
                        .chatId(chatId)
                        .text("Вы просмотрели все доступные вакансии на данный момент.")
                        .parseMode("HTML")
                        .build();
                telegramClient.execute(endMessage);
                return;
            }

            int toIndex = Math.min(fromIndex + CHAT_PAGE_SIZE, allVacancies.size());
            List<VacancyWrapper> pageVacancies = allVacancies.subList(fromIndex, toIndex);

            StringBuilder messageBuilder = new StringBuilder(page == 1
                    ? "<b>Вот свежие вакансии по вашим параметрам:</b>\n\n"
                    : "<b>Продолжение списка вакансий (Страница " + page + "):</b>\n\n");

            for (VacancyWrapper wrapper : pageVacancies) {
                Vacancy vacancy = wrapper.getVacancy();
                if (vacancy != null) {
                    messageBuilder.append(vacancy).append("\n").repeat("—", 20).append("\n");
                }
            }

            boolean hasNext = (toIndex == fromIndex + CHAT_PAGE_SIZE);
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(messageBuilder.toString())
                    .parseMode("HTML")
                    .replyMarkup(KeyboardUtils.createPaginationKeyboard(page, hasNext))
                    .build();
            telegramClient.execute(sendMessage);
        } catch (Exception e) {
            logger.error("Ошибка пагинации вакансий: {}", e.getMessage(), e);
        }
    }

    public void cancelDailyMessage(String chatId) {
        ScheduledFuture<?> future = activeTasks.remove(chatId);
        if (future != null) {
            future.cancel(false);
            logger.info("Запланированное событие у пользователя: {} было удалено", chatId);
            if (apiResponseRepository.exists(Long.valueOf(chatId))) {
                apiResponseRepository.delete(Long.valueOf(chatId));
            }
        }
    }

    public void restoreTasks(UserRepository userRepository) {
        List<User> allUsers = userRepository.getAllUser();
        for (User user : allUsers) {
            UserSettings settings = user.getUserSettings();
            if (settings != null && settings.getNotificationTime() != null) {
                try {
                    int hour = settings.getNotificationTime().getHour();
                    int minute = settings.getNotificationTime().getMinute();
                    String rawZone = settings.getTimeZone() != null ? settings.getTimeZone() : "GMT+3";
                    if (rawZone.startsWith("+") || rawZone.startsWith("-")) rawZone = "GMT" + rawZone;
                    ZoneId userZoneId = ZoneId.of(rawZone);

                    this.scheduleDailyMessage(String.valueOf(user.getChatId()), hour, minute, userZoneId);
                } catch (Exception e) {
                    logger.error("Ошибка восстановления для пользователя {}: {}", user.getChatId(), e.getMessage());
                }
            }
        }
    }

    private void sendMessageNotFoundVacancy(String chatId) throws TelegramApiException {
        SendMessage noVacanciesMessage = SendMessage.builder()
                .chatId(chatId)
                .text("По вашим параметрам сегодня новых вакансий не найдено.")
                .parseMode("HTML")
                .build();
        telegramClient.execute(noVacanciesMessage);
    }

    private void sendAlertMessage(String chatId) throws TelegramApiException {
        SendMessage alertMessage = SendMessage.builder()
                .chatId(chatId)
                .text("⚠️ Поиск слишком обширный (по всей России без ключевых слов). " +
                        "Сервер не может отобразить больше 100 вакансий.\n\n" +
                        "Пожалуйста, укажите <b>ключевое слово</b> или <b>регион</b> в настройках, " +
                        "чтобы листать дальше!")
                .parseMode("HTML")
                .replyMarkup(KeyboardUtils.getOneButtonKeyboard("Меню",
                        Command.MENU.getText()))
                .build();
        telegramClient.execute(alertMessage);
    }
}
