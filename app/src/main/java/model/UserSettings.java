package model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

@Data
public class UserSettings implements Serializable {
    private String timeZone = "+03:00";
    private Integer region = null;
    private Integer experience = null;
    private Long salary = null;
    private String keyword = "";
    private LocalTime notificationTime = null;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (timeZone != null && !timeZone.isEmpty()) {
            sb.append("Часовой пояс: ").append(timeZone).append(System.lineSeparator());
        }
        if (region != null) {
            sb.append("Регион: ").append(region).append(System.lineSeparator());
        }
        if (experience != null) {
            sb.append("Опыт: ").append(experience < 0 ? " не учитывать" : experience).append(System.lineSeparator());
        }
        if (salary != null) {
            sb.append("Зарплата: ").append(salary).append(System.lineSeparator());
        }
        if (keyword != null && !keyword.isEmpty()) {
            sb.append("Ключевые слова: ").append(keyword).append(System.lineSeparator());
        }
        if (notificationTime != null) {
            sb.append("Время для уведомления: ").append(notificationTime).append(System.lineSeparator());
        }
        return sb.isEmpty() ? "Фильтры не заданы" : sb.toString();
    }
}
