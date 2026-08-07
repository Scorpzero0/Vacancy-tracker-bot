package repository;

import model.ApiResponse;
import java.util.Optional;

public interface ApiResponseRepository {

    boolean exists(Long id);
    Optional<ApiResponse> getApiResponse(Long id);
    Optional<ApiResponse> readApiResponse(String jsonFile);
    void save(ApiResponse apiResponse, long userId);
    void update(ApiResponse apiResponse, long userId);
    void delete(Long id);

}
