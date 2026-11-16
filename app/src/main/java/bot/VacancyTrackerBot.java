    package bot;

    import bot.command.BotCommandExecutor;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
    import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
    import org.telegram.telegrambots.meta.api.objects.Update;
    import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
    import org.telegram.telegrambots.meta.generics.TelegramClient;
    import repository.UserRepository;

    public class VacancyTrackerBot implements LongPollingSingleThreadUpdateConsumer {

        private final TelegramClient telegramClient;
        private final UserRepository userRepository;
        private final BotCommandExecutor botCommandExecutor;
        private static final Logger logger = LoggerFactory.getLogger(VacancyTrackerBot.class);

        public VacancyTrackerBot(TelegramClient telegramClient, UserRepository userRepository,
                                 BotCommandExecutor botCommandExecutor) {
            this.telegramClient = telegramClient;
            this.userRepository = userRepository;
            this.botCommandExecutor = botCommandExecutor;
        }

        @Override
        public void consume(Update update) {
            if (!update.hasMessage() && !update.hasCallbackQuery()) {
                logger.info("No message or text in message");
            }
            BotApiMethod<?> message = botCommandExecutor.commandHandle(update, userRepository);
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                logger.error(e.getMessage());
            }
        }
    }
