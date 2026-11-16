import bot.VacancyTrackerBot;
import bot.command.*;
import bot.scheduler.BotSchedulerManager;
import config.Config;
import config.ConfigReaderEnvironment;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import repository.JsonUserRepository;
import repository.JsonApiResponseRepository;
import service.InputTransformService;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        Config config = new ConfigReaderEnvironment().read();
        String botName = config.botName();
        String botToken = config.botToken();

        var telegramClient = new OkHttpTelegramClient(botToken);
        var userRepository = new JsonUserRepository();
        var vacancyRepository = new JsonApiResponseRepository();
        BotSchedulerManager botSchedulerManager = new BotSchedulerManager(telegramClient, userRepository, vacancyRepository);

        botSchedulerManager.restoreTasks(userRepository);

        List<BotCommand> botCommands = List.of(new StartCommand(),
                new MenuCommand(), new MenuCommand(), new RegionCommand(),
                new ExperienceCommand(), new ExperienceCommand(), new SalaryCommand(), new KeywordCommand(),
                new NotificationCommand(), new ReadyCommand(botSchedulerManager), new StopCommand(botSchedulerManager),
                new UnknowCommand(), new UtcCommand(), new VacancyPageCommand(botSchedulerManager));
        InputTransformService inputTransformService = new InputTransformService();
        BotCommandExecutor botCommandExecutor = new BotCommandExecutor(botCommands, inputTransformService);
        try (var botsApplication = new TelegramBotsLongPollingApplication()){
            botsApplication.registerBot(botToken, new VacancyTrackerBot(telegramClient, userRepository, botCommandExecutor));
            System.out.println("Bot is running!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
