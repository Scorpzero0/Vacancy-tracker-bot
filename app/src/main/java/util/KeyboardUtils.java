package util;

import bot.command.Command;
import model.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import java.util.ArrayList;

public final class KeyboardUtils {

    private KeyboardUtils() {
        /* This utility class should not be instantiated */
    }

    public static InlineKeyboardMarkup getKeyboard(String[] buttonCommands, String[] buttonNames) {
        var rowList = new ArrayList<InlineKeyboardRow>();
        for (int i = 0; i < buttonCommands.length; i++) {
            var button = new InlineKeyboardButton(buttonNames[i]);
            button.setCallbackData(buttonCommands[i]);
            var row = new InlineKeyboardRow();
            row.add(button);
            rowList.add(row);
        }
        return new InlineKeyboardMarkup(rowList);
    }

    public static String[] getDynamicButtonNames(User user) {
        String region = (user != null && user.getUserSettings().getRegion() != null) ?
                String.valueOf(user.getUserSettings().getRegion()) : "";
        String experience = (user != null && user.getUserSettings().getExperience() != null) ?
                String.valueOf(user.getUserSettings().getExperience()) : "";
        String salary = (user != null && user.getUserSettings().getSalary() != null) ?
                String.valueOf(user.getUserSettings().getSalary()) : "";
        String keyword = (user != null && user.getUserSettings().getKeyword() != null) ?
                user.getUserSettings().getKeyword() : "";
        String notification = (user != null && user.getUserSettings().getNotificationTime() != null) ?
                String.valueOf(user.getUserSettings().getNotificationTime()) : "";

        return new String[]{
                "Регион" + (region.isEmpty() ? "" : " [" + region + "]"),
                "Минимальный опыт" + (experience.isEmpty() ? "" : " [" + experience + "]"),
                "Минимальная зарплата" + (salary.isEmpty() ? "" : " [" + salary + "]"),
                "Слова для поиска" + (keyword.isEmpty() ? "" : " [" + keyword + "]"),
                "Настройки нотификации" + (notification.isEmpty() ? "" : " [" + notification + "]"),
                "Готово"
        };
    }

    public static InlineKeyboardMarkup getOneButtonKeyboard(String buttonText, String buttonCallbackData){
        var button = new InlineKeyboardButton(buttonText);
        button.setCallbackData(buttonCallbackData);
        var row = new InlineKeyboardRow();
        row.add(button);
        var rowList = new ArrayList<InlineKeyboardRow>();
        rowList.add(row);
        return new InlineKeyboardMarkup(rowList);
    }

    public static InlineKeyboardMarkup getRegionKeyboard(int startIndex) {
        var rowList = new ArrayList<InlineKeyboardRow>();
        RegionDirectory[] regions = RegionDirectory.values();
        if (startIndex < 0) startIndex = 0;
        if (startIndex >= regions.length) startIndex = Math.max(0, regions.length - 10);
        int endIndex = Math.min(startIndex + 10, regions.length);
        for (int i = startIndex; i < endIndex; i++) {
            RegionDirectory region = regions[i];
            var button = new InlineKeyboardButton(region.getCode() + " " + region.getName());
            button.setCallbackData(Command.REGION.getText() + " " + region.getCode());
            rowList.add(new InlineKeyboardRow(button));
        }
        var navigationRow = new InlineKeyboardRow();
        if (startIndex > 0) {
            var prevButton = new InlineKeyboardButton("◀️ Прошлые");
            prevButton.setCallbackData(Command.REGION.getText() + " page " + (startIndex - 10));
            navigationRow.add(prevButton);
        }
        if (endIndex < regions.length) {
            var nextButton = new InlineKeyboardButton("Следующие ▶️");
            nextButton.setCallbackData(Command.REGION.getText() + " page " + endIndex);
            navigationRow.add(nextButton);
        }
        if (!navigationRow.isEmpty()) {
            rowList.add(navigationRow);
        }
        rowList.add(getMenuButton());
        return new InlineKeyboardMarkup(rowList);
    }

    public static InlineKeyboardMarkup getCheckDeleteKeyboard() {
        var yesButton = new InlineKeyboardButton("Да");
        yesButton.setCallbackData("/stop yes");
        var noButton = new InlineKeyboardButton("Нет");
        noButton.setCallbackData("/stop no");
        var row = new InlineKeyboardRow();
        row.add(yesButton);
        row.add(noButton);
        var rowList = new ArrayList<InlineKeyboardRow>();
        rowList.add(row);
        return new InlineKeyboardMarkup(rowList);
    }

    public static InlineKeyboardMarkup createPaginationKeyboard(int page, boolean hasNext) {
        InlineKeyboardRow paginationRow = new InlineKeyboardRow();
        if (page > 1) {
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("⬅️ Предыдущие")
                    .callbackData("vac_page_" + (page - 1))
                    .build());
        }
        if (hasNext) {
            paginationRow.add(InlineKeyboardButton.builder()
                    .text("Следующие ➡️")
                    .callbackData("vac_page_" + (page + 1))
                    .build());
        }
        var keyboardBuilder = InlineKeyboardMarkup.builder();
        if (!paginationRow.isEmpty()) {
            keyboardBuilder.keyboardRow(paginationRow);
        }
        keyboardBuilder.keyboardRow(getMenuButton());
        return keyboardBuilder.build();
    }

    private static InlineKeyboardRow getMenuButton() {
        InlineKeyboardRow menuRow = new InlineKeyboardRow();
        menuRow.add(InlineKeyboardButton.builder()
                .text("🏠 Главное меню")
                .callbackData("/menu")
                .build());
        return menuRow;
    }
}
