package service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exception.FilesWorkException;
import exception.NotFoundException;
import model.ApiResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ApiResponseReader {

    private final String userDataDir;
    private static final String FILE_FORMAT = ".json";
    public final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ApiResponseReader() {
        this("data/responses/");
    }

    public ApiResponseReader(String userDataDir) {
        this.userDataDir = userDataDir;
        new File(this.userDataDir).mkdirs();
    }

    public boolean exists(Long id) {
        String fileName = userDataDir + id + FILE_FORMAT;
        return Files.exists(Path.of(fileName));
    }

    public Optional<ApiResponse> getApiResponse(Long id) {
        String fileName = userDataDir + id + FILE_FORMAT;
        if (exists(id)) {
            ApiResponse apiResponse;
            try {
                apiResponse = objectMapper.readValue(new File(fileName), ApiResponse.class);
            } catch (IOException e) {
                throw new FilesWorkException(e.getMessage());
            }
            return Optional.of(apiResponse);
        }
        return Optional.empty();
    }

    public Optional<ApiResponse> readApiResponse(String jsonFile) {
        ApiResponse apiResponse;
        try {
            apiResponse = objectMapper.readValue(jsonFile, ApiResponse.class);
        } catch (IOException e) {
            throw new FilesWorkException("Read file error");
        }
        return Optional.of(apiResponse);
    }

    public void writeApiResponseData(ApiResponse apiResponse, long userId) {
        if (apiResponse == null) {
            throw new IllegalArgumentException("Incorrect update vacancy data");
        }
        String fileName = userDataDir + userId + FILE_FORMAT;
        try {
            objectMapper.writeValue(new File(fileName), apiResponse);
        } catch (IOException e) {
            throw new FilesWorkException(e.getMessage());
        }
    }

    public void updateApiResponse(ApiResponse apiResponse, long userId) {
        if (apiResponse == null) {
            throw new IllegalArgumentException("Incorrect update vacancy data");
        }
        if (!exists(userId)) {
            throw new NotFoundException("Vacancy: " + userId + " not found");
        }
        writeApiResponseData(apiResponse, userId);
    }

    public void deleteApiResponseData(Long id) {
        String fileName = userDataDir + id + FILE_FORMAT;
        if (!exists(id)) {
            throw new NotFoundException("File with id: " + id + " not found");
        } else {
            try {
                Files.delete(Path.of(fileName));
            } catch (IOException e) {
                throw new FilesWorkException(e.getMessage());
            }
        }
    }
}
