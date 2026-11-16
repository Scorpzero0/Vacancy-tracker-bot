package config;

public class ConfigReaderEnvironment implements ConfigReader{

    @Override
    public Config read() {
        String botName = System.getenv("BOT_NAME");
        String botToken = System.getenv("BOT_TOKEN");
        return new Config(botName, botToken);
    }
}
