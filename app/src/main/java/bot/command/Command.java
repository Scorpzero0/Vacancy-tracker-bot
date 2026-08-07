package bot.command;

import lombok.Getter;

@Getter
public enum Command {
    UTC("/UTC"),
    START("/start"),
    MENU("/menu"),
    REGION("/region"),
    EXPERIENCE("/experience"),
    SALARY("/salary"),
    KEYWORD("/keyword"),
    NOTIFICATION("/notify"),
    READY("/ready"),
    STOP("/stop"),
    UNKNOWN("");

    private final String text;

    Command(String text) {
        this.text = text;
    }
}
