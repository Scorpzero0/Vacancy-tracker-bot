package util;

import bot.command.AbstractCommandTest;
import model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyboardUtilsTest extends AbstractCommandTest {

    @Test
    void givenUserWithParams_whenGetDynamicButtonNames_thenFixedButtonNames() {
        User user = createTestUserWithUserSettings();
        assertEquals(getExpectedResultWithParams().length, KeyboardUtils.getDynamicButtonNames(user).length);
        assertArrayEquals(getExpectedResultWithParams(), KeyboardUtils.getDynamicButtonNames(user));
    }

    @Test
    void givenUserWithoutParams_whenGetDynamicButtonNames_thenButtonNames() {
        User user = null;
        assertEquals(getExpectedResultWithoutParams().length, KeyboardUtils.getDynamicButtonNames(user).length);
        assertArrayEquals(getExpectedResultWithoutParams(), KeyboardUtils.getDynamicButtonNames(user));
    }

    private String[] getExpectedResultWithParams() {
        return new String[]{
                "Регион [50]",
                "Минимальный опыт [5]",
                "Минимальная зарплата [50000]",
                "Слова для поиска [Test]",
                "Настройки нотификации [12:30]",
                "Готово"
        };
    }

    private String[] getExpectedResultWithoutParams() {
        return new String[]{
                "Регион",
                "Минимальный опыт",
                "Минимальная зарплата",
                "Слова для поиска",
                "Настройки нотификации",
                "Готово"
        };
    }
}
