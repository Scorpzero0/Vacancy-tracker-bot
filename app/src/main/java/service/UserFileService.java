package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exception.FilesWorkException;
import exception.NotFoundException;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserFileService {

    private static final Logger logger = LoggerFactory.getLogger(UserFileService.class);
    private final String userDataDir;
    private static final String FILE_FORMAT = ".json";
    public final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public UserFileService() {
        this("data/users/");
    }

    public UserFileService(String userDataDir) {
        this.userDataDir = userDataDir;
        new File(this.userDataDir).mkdirs();
    }

    public Optional<User> readAndGetUser(Long id) {
        String fileName = userDataDir + id + FILE_FORMAT;
        if (exists(id)) {
            User uploadUser;
            try {
                uploadUser = objectMapper.readValue(new File(fileName), User.class);
            } catch (IOException e) {
                throw new FilesWorkException(e.getMessage());
            }
            return Optional.of(uploadUser);
        }
        return Optional.empty();
    }

    public boolean exists(Long id) {
        String fileName = userDataDir + id + FILE_FORMAT;
        return Files.exists(Path.of(fileName));
    }

    public void writeUserData(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Incorrect update user data");
        }
        String fileName = userDataDir + user.getUserId() + FILE_FORMAT;
        try {
            objectMapper.writeValue(new File(fileName), user);
        } catch (IOException e) {
            throw new FilesWorkException(e.getMessage());
        }
    }

    public void updateUser(User updateUser) {
        if (updateUser == null) {
            throw new IllegalArgumentException("Incorrect update user data");
        }
        if (!exists(updateUser.getChatId())) {
            throw new NotFoundException("User: " + updateUser.getChatId() + " not found");
        }
        writeUserData(updateUser);
    }


    public void deleteUserData(Long id) {
        String fileName = userDataDir + id + FILE_FORMAT;
        if (!exists(id)) {
            throw new NotFoundException("User with id: " + id + " not found");
        } else {
            try {
                Files.delete(Path.of(fileName));
            } catch (IOException e) {
                throw new FilesWorkException(e.getMessage());
            }
        }
    }

    public List<User> readAllUsers() {
        Path path = Paths.get(userDataDir);
        if (!Files.exists(path)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(path, 1)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(FILE_FORMAT))
                    .map(p -> {
                        try {
                            return objectMapper.readValue(p.toFile(), User.class);
                        } catch (IOException e) {
                            logger.error("Ошибка чтения JSON-файла пользователя {}: {}",
                                    p.getFileName(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Ошибка при сканировании папки пользователей: {}", e.getMessage());
            return List.of();
        }
    }
}
