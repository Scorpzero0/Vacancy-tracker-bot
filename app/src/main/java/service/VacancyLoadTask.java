package service;

import lombok.Setter;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.UserRepository;
import repository.ApiResponseRepository;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VacancyLoadTask implements IdentifiableTask {

    private static final Logger logger = LoggerFactory.getLogger(VacancyLoadTask.class);
    private static final String BASE_URL = "https://opendata.trudvsem.ru/api/v1/vacancies";
    private static final String ISO_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String PATH = "data/responses/";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(ISO_FORMAT_PATTERN);
    private static final int PAGE_SIZE = 100;
    private final HttpClient httpClient;
    private final UserRepository userRepository;
    private final ApiResponseRepository apiResponseRepository;
    private final Long userId;

    public VacancyLoadTask(HttpClient httpClient, UserRepository userRepository, ApiResponseRepository apiResponseRepository, Long userId) {
        this.httpClient = httpClient;
        this.userRepository = userRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.userId = userId;

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Setter
    private int offset = 0;

    @Override
    public void run() {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден с ID: " + userId));
        if (user.getUserSettings() == null) {
            throw new IllegalStateException("У пользователя не заданы настройки поиска вакансий");
        }
        Long userMinSalary = user.getUserSettings().getSalary();
        long minSalary = (userMinSalary != null && userMinSalary > 0) ? userMinSalary : 0L;
        long maxSalary = Long.MAX_VALUE;
        String fullUrl = buildQuery(user);
        logger.info("Попытка запроса к API TrudVsem. URL: {}", fullUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                String json = response.body();
                Optional<ApiResponse> apiResponseOpt = apiResponseRepository.readApiResponse(json);

                if (apiResponseOpt.isPresent()) {
                    ApiResponse newResponse = apiResponseOpt.get();
                    Optional<ApiResponse> existingResponseOpt = apiResponseRepository.getApiResponse(userId);

                    if (existingResponseOpt.isPresent() && existingResponseOpt.get().getResults() != null
                            && existingResponseOpt.get().getResults().getVacancies() != null) {

                        List<VacancyWrapper> existingVacancies = existingResponseOpt.get().getResults().getVacancies();
                        List<VacancyWrapper> newVacancies = newResponse.getResults() != null
                                ? newResponse.getResults().getVacancies()
                                : List.of();

                        Set<String> existingIds = existingVacancies.stream()
                                .map(w -> w.getVacancy() != null ? w.getVacancy().getId() : null)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());
                        List<VacancyWrapper> uniqueNewVacancies = newVacancies.stream()
                                .filter(w -> w.getVacancy() != null)
                                // Проверка на уникальность
                                .filter(w -> !existingIds.contains(w.getVacancy().getId()))
                                // Проверка диапазона зарплат (Вызов нового метода)
                                .filter(w -> isSalaryInBounds(w.getVacancy(), minSalary, maxSalary))
                                .toList();
                        existingVacancies.addAll(uniqueNewVacancies);
                        apiResponseRepository.save(existingResponseOpt.get(), userId);
                        logger.info("Добавлено {} новых уникальных вакансий с подходящей ЗП для пользователя {}",
                                uniqueNewVacancies.size(), userId);
                    } else {
                        if (newResponse.getResults() != null && newResponse.getResults().getVacancies() != null) {
                            List<VacancyWrapper> filteredVacancies = newResponse.getResults().getVacancies().stream()
                                    .filter(w -> w.getVacancy() != null)
                                    .filter(w -> isSalaryInBounds(w.getVacancy(), minSalary, maxSalary))
                                    .collect(Collectors.toList());
                            newResponse.getResults().setVacancies(filteredVacancies);
                        }
                        apiResponseRepository.save(newResponse, userId);
                    }
                }
            } else {
                logger.error("API TrudVsem вернул код ошибки: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ошибка загрузки вакансий для пользователя " + userId, e);
        }
    }

    public String buildQuery(User user) {
        UserSettings settings = user.getUserSettings();
        Integer region = settings.getRegion();
        if (region != null && region == 0) {
            region = null;
        }
        Integer experienceFrom = settings.getExperience();
        if (experienceFrom != null && experienceFrom < 0) {
            experienceFrom = null;
        }
        Long salaryFrom = settings.getSalary();
        if (salaryFrom != null && salaryFrom < 0) {
            salaryFrom = null;
        }
        String keyword = settings.getKeyword();
        String modifiedFrom = LocalDateTime.now().minusDays(7).format(ISO_FORMATTER);
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        if (region != null) {
            urlBuilder.append("/region/").append(region);
        }
        String queryParams = buildParams(experienceFrom, salaryFrom, keyword, modifiedFrom);
        urlBuilder.append(queryParams).append("&offset=").append(this.offset);

        return urlBuilder.toString();
    }

    private String buildParams(Integer experienceFrom, Long salaryFrom, String keyword, String modifiedFrom) {
        return Stream.of(
                        formatParam("experienceFrom", experienceFrom),
                        formatParam("salaryFrom", salaryFrom),
                        formatParam("text", (keyword != null && !keyword.isEmpty()) ? keyword : null),
                        formatParam("modifiedFrom", modifiedFrom),
                        formatParam("limit", PAGE_SIZE)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining("&", "?", ""));
    }

    private String formatParam(String key, Object value) {
        return (value == null)
                ? null
                : key + "=" + URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public Long getId() {
        return userId;
    }

    private boolean isSalaryInBounds(Vacancy v, long minSalary, long maxSalary) {
        if (v.getSalaryMin() == null && v.getSalaryMax() == null) {
            return false;
        }
        if (v.getSalaryMin() != null && v.getSalaryMin() < minSalary) {
            return false;
        }
        if (v.getSalaryMax() != null && v.getSalaryMax() > maxSalary) {
            return false;
        }
        return true;
    }
}
