package repository;

import model.ApiResponse;
import service.ApiResponseReader;
import java.util.Optional;

public class JsonApiResponseRepository implements ApiResponseRepository {

    ApiResponseReader apiResponseReader = new ApiResponseReader();

    @Override
    public boolean exists(Long id) {
        return apiResponseReader.exists(id);
    }

    @Override
    public Optional<ApiResponse> getApiResponse(Long id) {
        return apiResponseReader.getApiResponse(id);
    }

    @Override
    public Optional<ApiResponse> readApiResponse(String jsonFile) {
        return apiResponseReader.readApiResponse(jsonFile);
    }

    @Override
    public void save(ApiResponse apiResponse, long userId) {
        apiResponseReader.writeApiResponseData(apiResponse, userId);
    }

    @Override
    public void update(ApiResponse apiResponse, long userId) {
        apiResponseReader.updateApiResponse(apiResponse, userId);
    }

    @Override
    public void delete(Long id) {
        apiResponseReader.deleteApiResponseData(id);
    }
}
