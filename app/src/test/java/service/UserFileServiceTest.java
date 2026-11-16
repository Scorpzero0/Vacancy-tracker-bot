package service;

import bot.command.AbstractCommandTest;
import exception.FilesWorkException;
import exception.NotFoundException;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserFileServiceTest extends AbstractCommandTest {

    private UserFileService service;
    private User user;
    private String dynamicUserDir;

    private static final String FILE_FORMAT = ".json";

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        Path usersTempPath = tempDir.resolve("users/");
        Files.createDirectories(usersTempPath);
        dynamicUserDir = usersTempPath.toAbsolutePath() + File.separator;
        service = new UserFileService(dynamicUserDir);
        user = createTestUserWithUserSettings();
        service.writeUserData(user);
    }

    @Test
    void givenUser_whenWriteUserData_thenNewFile() {
        Path filePath = Path.of(dynamicUserDir + user.getUserId() + FILE_FORMAT);
        assertTrue(Files.exists(filePath));
    }

    @Test
    void givenUser_whenReadAndGetUser_thenGetUser() {
        Optional<User> actual = service.readAndGetUser(userId);
        assertTrue(actual.isPresent());
        assertEquals(user.getUserId(), actual.get().getUserId());
    }

    @Test
    void givenEmptyUser_whenReadAndGetUser_thenEmpty() {
        Optional<User> result = service.readAndGetUser(99999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenUser_whenExists_thenTrue() {
        assertTrue(service.exists(userId));
    }

    @Test
    void givenEmptyUser_whenExists_thenFalse() {
        assertFalse(service.exists(99999L));
    }

    @Test
    void givenInvalidFileContent_whenReadAndGetUser_thenException() throws IOException {
        Path filePath = Path.of(dynamicUserDir + "888" + FILE_FORMAT);
        Files.writeString(filePath, "{invalid-json-format}");
        assertThrows(FilesWorkException.class, () -> service.readAndGetUser(888L));
    }

    @Test
    void givenUserId_whenDeleteUserData_thenDeleteFile() {
        Path filePath = Path.of(dynamicUserDir + user.getUserId() + FILE_FORMAT);
        assertTrue(Files.exists(filePath));
        service.deleteUserData(user.getUserId());
        assertFalse(Files.exists(filePath));
    }

    @Test
    void givenInvalidUserId_whenDeleteUserData_thenException() {
        assertThrows(NotFoundException.class, () -> service.deleteUserData(99999L));
    }

    @Test
    void givenUserId_whenUpdateUserData_thenUpdate() {
        assertDoesNotThrow(() -> service.updateUser(user));
    }

    @Test
    void givenEmptyUser_whenUpdateUserData_thenException() {
        assertThrows(IllegalArgumentException.class, () -> service.updateUser(null));
    }

    @Test
    void givenNotPresentUser_whenUpdateUserData_thenException() {
        User nonExistentUser = createTestUserWithoutUserSettings();
        nonExistentUser.setChatId(77777L);
        assertThrows(NotFoundException.class, () -> service.updateUser(nonExistentUser));
    }

    @Test
    void given_whenReadAllUsers_thenUsersList() {
        User user1 = createTestUserWithUserSettings();
        user1.setUserId(101L);
        User user2 = createTestUserWithUserSettings();
        user2.setUserId(102L);
        service.writeUserData(user1);
        service.writeUserData(user2);
        List<User> users = service.readAllUsers();
        assertNotNull(users);
        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(u -> u.getUserId() == 101L));
        assertTrue(users.stream().anyMatch(u -> u.getUserId() == 102L));
    }

    @Test
    void givenCorrectAndInvalidFiles_whenReadAllUsers_thenReadCorrectFile() throws IOException {
        User validUser = createTestUserWithUserSettings();
        validUser.setUserId(201L);
        service.writeUserData(validUser);
        Path corruptedPath = Path.of(dynamicUserDir + "202" + FILE_FORMAT);
        Files.writeString(corruptedPath, "{corrupted broken json}");
        List<User> users = service.readAllUsers();
        assertNotNull(users);
        assertTrue(users.stream().anyMatch(u -> u.getUserId() == 201L));
        assertFalse(users.stream().anyMatch(u -> u.getUserId() == 202L));
    }
}
