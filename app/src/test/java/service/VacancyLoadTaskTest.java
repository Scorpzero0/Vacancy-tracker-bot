package service;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.ApiResponseRepository;
import repository.UserRepository;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyLoadTaskTest {

    private VacancyLoadTask loadTask;
    @Mock
    private HttpClient httpClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApiResponseRepository apiResponseRepository;
    @Mock
    private HttpResponse<String> mockHttpResponse;
    private final Long TEST_USER_ID = 777L;

    @BeforeEach
    void setUp() {
        loadTask = new VacancyLoadTask(httpClient, userRepository, apiResponseRepository, TEST_USER_ID);
    }

    @Test
    void givenUserSettingsWithDefaultOrNegativeValues_whenBuildQuery_thenIgnoreThem() {
        User user = new User();
        UserSettings settings = new UserSettings();
        settings.setRegion(0);
        settings.setExperience(-5);
        settings.setSalary(-1000L);
        settings.setKeyword("");
        user.setUserSettings(settings);
        String url = loadTask.buildQuery(user);
        assertFalse(url.contains("region"));
        assertFalse(url.contains("experienceFrom"));
        assertFalse(url.contains("salaryFrom"));
        assertTrue(url.contains("limit=100"));
        assertTrue(url.contains("offset=0"));
    }

    @Test
    void givenTargetedUserSettings_whenBuildQuery_thenCreateFullCorrectUrl() {
        User user = new User();
        UserSettings settings = new UserSettings();
        settings.setRegion(50);
        settings.setExperience(3);
        settings.setSalary(70000L);
        settings.setKeyword("Java Developer");
        user.setUserSettings(settings);
        loadTask.setOffset(200);
        String url = loadTask.buildQuery(user);
        assertTrue(url.contains("/region/50"));
        assertTrue(url.contains("experienceFrom=3"));
        assertTrue(url.contains("salaryFrom=70000"));
        assertTrue(url.contains("text=Java"));
        assertTrue(url.contains("offset=200"));
    }

    @Test
    void givenNoUserInRepository_whenRun_thenThrowIllegalArgumentException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> loadTask.run());
    }

    @Test
    void givenUserWithoutSettings_whenRun_thenThrowIllegalStateException() {
        User user = new User();
        user.setUserSettings(null);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> loadTask.run());
    }

    @Test
    void givenNewUser_whenRunAndApiReturns200_thenSaveFreshApiResponse() throws Exception {
        User user = new User();
        user.setUserSettings(new UserSettings());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        String fakeJson = "{ \"results\": { \"vacancies\": [] } }";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(fakeJson);
        doReturn(mockHttpResponse).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        ApiResponse freshResponse = new ApiResponse();
        when(apiResponseRepository.readApiResponse(fakeJson)).thenReturn(Optional.of(freshResponse));
        when(apiResponseRepository.getApiResponse(TEST_USER_ID)).thenReturn(Optional.empty());
        loadTask.run();
        verify(apiResponseRepository, times(1)).save(freshResponse, TEST_USER_ID);
    }

    @Test
    void givenExistingUser_whenRun_thenMergeOnlyUniqueNewVacancies() throws Exception {
        User user = new User();
        user.setUserSettings(new UserSettings());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        String fakeJson = "{ \"results\": { \"vacancies\": [...] } }";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(fakeJson);
        doReturn(mockHttpResponse).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        ApiResponse existingResponse = new ApiResponse();
        Results existingResults = new Results();
        List<VacancyWrapper> existingList = new ArrayList<>();
        existingList.add(createVacancyWrapper("1"));
        existingList.add(createVacancyWrapper("2"));
        existingResults.setVacancies(existingList);
        existingResponse.setResults(existingResults);
        ApiResponse freshResponse = new ApiResponse();
        Results freshResults = new Results();
        List<VacancyWrapper> freshList = new ArrayList<>();
        freshList.add(createVacancyWrapper("2"));
        freshList.add(createVacancyWrapper("3"));
        freshResults.setVacancies(freshList);
        freshResponse.setResults(freshResults);
        when(apiResponseRepository.readApiResponse(fakeJson)).thenReturn(Optional.of(freshResponse));
        when(apiResponseRepository.getApiResponse(TEST_USER_ID)).thenReturn(Optional.of(existingResponse));
        loadTask.run();
        assertEquals(3, existingResponse.getResults().getVacancies().size());
        verify(apiResponseRepository, times(1)).save(existingResponse, TEST_USER_ID);
    }

    @Test
    void givenApiError404_whenRun_thenDoNotSaveAnything() throws Exception {
        User user = new User();
        user.setUserSettings(new UserSettings());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(mockHttpResponse.statusCode()).thenReturn(404);
        doReturn(mockHttpResponse).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        loadTask.run();
        verify(apiResponseRepository, never()).readApiResponse(anyString());
        verify(apiResponseRepository, never()).save(any(), anyLong());
    }

    private VacancyWrapper createVacancyWrapper(String id) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(id);
        vacancy.setSalaryMin(50000);
        vacancy.setSalaryMax(100000);
        VacancyWrapper wrapper = new VacancyWrapper();
        wrapper.setVacancy(vacancy);
        return wrapper;
    }
}
