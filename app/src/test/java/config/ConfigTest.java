package config;

import exception.InvalidBotTokenException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ConfigTest {

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "null"
    }, nullValues = "null")
    void givenInvalidBotToken_whenConfig_thenException(String botToken) {
        assertThrows(InvalidBotTokenException.class, () -> new Config("test", botToken));
    }
}
