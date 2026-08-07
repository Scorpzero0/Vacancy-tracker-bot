package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import exception.FilesWorkException;
import exception.NotFoundException;
import model.ApiResponse;
import model.Vacancy;
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

class ApiResponseReaderTest {
    private ApiResponseReader reader;
    private ObjectMapper mapper;

    @TempDir
    Path tempDir;
    private final Long TEST_ID = 999L;

    @BeforeEach
    void setUp() {
        reader = new ApiResponseReader(tempDir.toString() + File.separator);
        mapper = reader.objectMapper;
    }

    @Test
    void givenNoFile_whenExists_thenReturnFalse() {
        assertFalse(reader.exists(TEST_ID));
    }

    @Test
    void givenFileExists_whenExists_thenReturnTrue() throws IOException {
        Files.createFile(tempDir.resolve(TEST_ID + ".json"));
        assertTrue(reader.exists(TEST_ID));
    }

    @Test
    void givenNoFile_whenGetApiResponse_thenReturnEmptyOptional() {
        Optional<ApiResponse> result = reader.getApiResponse(TEST_ID);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenValidJsonFile_whenGetApiResponse_thenReturnData() throws IOException {
        ApiResponse expectedResponse = new ApiResponse();
        mapper.writeValue(tempDir.resolve(TEST_ID + ".json").toFile(), expectedResponse);
        Optional<ApiResponse> result = reader.getApiResponse(TEST_ID);
        assertTrue(result.isPresent());
    }

    @Test
    void givenCorruptedJsonFile_whenGetApiResponse_thenThrowFilesWorkException() throws IOException {
        Files.writeString(tempDir.resolve(TEST_ID + ".json"), "{ invalid json... }");
        assertThrows(FilesWorkException.class, () -> reader.getApiResponse(TEST_ID));
    }

    @Test
    void givenValidJsonString_whenReadApiResponse_thenReturnOptionalData() {
        String json = "{}";
        Optional<ApiResponse> result = reader.readApiResponse(json);
        assertTrue(result.isPresent());
    }

    @Test
    void givenInvalidJsonString_whenReadApiResponse_thenThrowFilesWorkException() {
        String json = "{ corrupted }";
        assertThrows(FilesWorkException.class, () -> reader.readApiResponse(json));
    }

    @Test
    void givenNullResponse_whenWriteApiResponseData_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> reader.writeApiResponseData(null, TEST_ID));
    }

    @Test
    void givenValidData_whenWriteApiResponseData_thenFileIsCreated() {
        ApiResponse apiResponse = new ApiResponse();
        reader.writeApiResponseData(apiResponse, TEST_ID);
        Path expectedFilePath = tempDir.resolve(TEST_ID + ".json");
        assertTrue(Files.exists(expectedFilePath));
    }

    @Test
    void givenNullResponse_whenUpdateApiResponse_thenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> reader.updateApiResponse(null, TEST_ID));
    }

    @Test
    void givenFileDoesNotExist_whenUpdateApiResponse_thenThrowNotFoundException() {
        ApiResponse apiResponse = new ApiResponse();
        assertThrows(NotFoundException.class, () -> reader.updateApiResponse(apiResponse, TEST_ID));
    }

    @Test
    void givenFileExists_whenUpdateApiResponse_thenFileIsUpdatedSuccessfully() throws IOException {
        Files.createFile(tempDir.resolve(TEST_ID + ".json"));
        ApiResponse apiResponse = new ApiResponse();
        assertDoesNotThrow(() -> reader.updateApiResponse(apiResponse, TEST_ID));
    }

    @Test
    void givenNoFile_whenDeleteApiResponseData_thenThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> reader.deleteApiResponseData(TEST_ID));
    }

    @Test
    void givenFileExists_whenDeleteApiResponseData_thenFileIsDeleted() throws IOException {
        Path file = Files.createFile(tempDir.resolve(TEST_ID + ".json"));
        assertTrue(Files.exists(file));
        reader.deleteApiResponseData(TEST_ID);
        assertFalse(Files.exists(file));
    }
}
