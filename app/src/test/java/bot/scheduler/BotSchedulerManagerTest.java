package bot.scheduler;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import repository.ApiResponseRepository;
import repository.UserRepository;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotSchedulerManagerTest {

    private BotSchedulerManager manager;

    @Mock
    private TelegramClient telegramClient;
    @Mock
    private ScheduledExecutorService scheduler;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApiResponseRepository apiResponseRepository;
    @Mock
    private ScheduledFuture<?> mockFuture;

    private final String TEST_CHAT_ID = "12345";
    private final long TEST_USER_ID = 12345L;
    private final ZoneId zoneId = ZoneId.of("GMT+3");

    @BeforeEach
    void setUp() {
        manager = new BotSchedulerManager(telegramClient, scheduler, userRepository, apiResponseRepository);
    }

    @Test
    void givenNoActiveTask_whenCancelDailyMessage_thenDoNothing() {
        manager.cancelDailyMessage(TEST_CHAT_ID);
        verify(apiResponseRepository, never()).delete(anyLong());
    }

    @Test
    void givenValidParams_whenScheduleDailyMessage_thenTaskIsScheduledSuccessfully() {
        doReturn(mockFuture).when(scheduler).scheduleAtFixedRate(
                any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.SECONDS)
        );
        manager.scheduleDailyMessage(TEST_CHAT_ID, 12, 0, zoneId);
        verify(scheduler, times(1)).scheduleAtFixedRate(
                any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void givenUserNotFound_whenSendVacanciesPage_thenNoTelegramInteraction() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> manager.sendVacanciesPage(TEST_CHAT_ID, 1));
        verifyNoInteractions(telegramClient);
    }

    @Test
    void givenEmptyApiResponse_whenSendVacanciesPage_thenSendMessageNotFoundVacancy() throws TelegramApiException {
        User user = new User(TEST_USER_ID, TEST_USER_ID, new UserSettings());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(apiResponseRepository.getApiResponse(TEST_USER_ID)).thenReturn(Optional.empty());
        Message mockMessage = mock(Message.class);
        doReturn(mockMessage).when(telegramClient).execute(any(SendMessage.class));
        manager.sendVacanciesPage(TEST_CHAT_ID, 1);
        ArgumentCaptor<BotApiMethod> captor = ArgumentCaptor.forClass(BotApiMethod.class);
        verify(telegramClient).execute(captor.capture());
        SendMessage sentMessage = (SendMessage) captor.getValue();
        assertEquals(TEST_CHAT_ID, sentMessage.getChatId());
        assertTrue(sentMessage.getText().contains("новых вакансий не найдено"));
    }

    @Test
    void givenPageWithinBounds_whenSendVacanciesPage_thenSendFormattedVacancies() throws TelegramApiException {
        User user = new User(TEST_USER_ID, TEST_USER_ID, new UserSettings());
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        ApiResponse apiResponse = mock(ApiResponse.class);
        Results results = mock(Results.class);
        VacancyWrapper wrapper = mock(VacancyWrapper.class);
        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.toString()).thenReturn("Senior Java Developer");
        when(wrapper.getVacancy()).thenReturn(vacancy);
        when(results.getVacancies()).thenReturn(List.of(wrapper));
        when(apiResponse.getResults()).thenReturn(results);
        when(apiResponseRepository.getApiResponse(TEST_USER_ID)).thenReturn(Optional.of(apiResponse));
        Message mockMessage = mock(Message.class);
        doReturn(mockMessage).when(telegramClient).execute(any(SendMessage.class));
        manager.sendVacanciesPage(TEST_CHAT_ID, 1);
        ArgumentCaptor<BotApiMethod> captor = ArgumentCaptor.forClass(BotApiMethod.class);
        verify(telegramClient).execute(captor.capture());
        SendMessage sentMessage = (SendMessage) captor.getValue();
        assertTrue(sentMessage.getText().contains("Senior Java Developer"));
        assertTrue(sentMessage.getText().contains("Вот свежие вакансии"));
    }

    @Test
    void givenPageOutOfBoundsAndGlobalSearch_whenSendVacanciesPage_thenSendAlertMessage() throws TelegramApiException {
        UserSettings globalSettings = new UserSettings();
        globalSettings.setKeyword("");
        globalSettings.setRegion(0);
        User user = new User(TEST_USER_ID, TEST_USER_ID, globalSettings);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        ApiResponse apiResponse = mock(ApiResponse.class);
        Results results = mock(Results.class);
        List<VacancyWrapper> mockList = new ArrayList<>(Collections.nCopies(15, mock(VacancyWrapper.class)));
        when(results.getVacancies()).thenReturn(mockList);
        when(apiResponse.getResults()).thenReturn(results);
        when(apiResponseRepository.getApiResponse(TEST_USER_ID)).thenReturn(Optional.of(apiResponse));
        Message mockMessage = mock(Message.class);
        doReturn(mockMessage).when(telegramClient).execute(any(SendMessage.class));
        manager.sendVacanciesPage(TEST_CHAT_ID, 3); // fromIndex = 20 > 15
        ArgumentCaptor<BotApiMethod> captor = ArgumentCaptor.forClass(BotApiMethod.class);
        verify(telegramClient).execute(captor.capture());
        SendMessage sentMessage = (SendMessage) captor.getValue();
        assertTrue(sentMessage.getText().contains("Поиск слишком обширный"));
    }

    @Test
    void givenAllVacanciesViewed_whenSendVacanciesPage_thenSendEndMessage() throws TelegramApiException {
        UserSettings targetedSettings = new UserSettings();
        targetedSettings.setKeyword("Kotlin");
        User user = new User(TEST_USER_ID, TEST_USER_ID, targetedSettings);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        ApiResponse apiResponse = mock(ApiResponse.class);
        Results results = mock(Results.class);
        List<VacancyWrapper> mockList = new ArrayList<>(Collections.nCopies(5, mock(VacancyWrapper.class)));
        when(results.getVacancies()).thenReturn(mockList);
        when(apiResponse.getResults()).thenReturn(results);
        when(apiResponseRepository.getApiResponse(TEST_USER_ID)).thenReturn(Optional.of(apiResponse));
        Message mockMessage = mock(Message.class);
        doReturn(mockMessage).when(telegramClient).execute(any(SendMessage.class));
        manager.sendVacanciesPage(TEST_CHAT_ID, 2); // fromIndex = 10 > 5
        ArgumentCaptor<BotApiMethod> captor = ArgumentCaptor.forClass(BotApiMethod.class);
        verify(telegramClient).execute(captor.capture());
        SendMessage sentMessage = (SendMessage) captor.getValue();
        assertEquals("Вы просмотрели все доступные вакансии на данный момент.", sentMessage.getText());
    }

    @Test
    void givenUsersWithSettings_whenRestoreTasks_thenScheduleTasksCorrectly() {
        User user = new User();
        user.setChatId(TEST_USER_ID);
        UserSettings settings = new UserSettings();
        settings.setNotificationTime(LocalTime.of(9, 15));
        settings.setTimeZone("+03:00");
        user.setUserSettings(settings);
        when(userRepository.getAllUser()).thenReturn(List.of(user));
        doReturn(mockFuture).when(scheduler).scheduleAtFixedRate(
                any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.SECONDS)
        );
        manager.restoreTasks(userRepository);
        verify(scheduler, times(1)).scheduleAtFixedRate(
                any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.SECONDS)
        );
    }
}
