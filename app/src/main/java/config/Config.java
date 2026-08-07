package config;

import exception.InvalidBotTokenException;

public record Config(
        String botName, String botToken) {

    public Config{
        if (botToken == null || botToken.isEmpty()) {
            throw new InvalidBotTokenException("Token can not be empty or null");
        }
    }
}

