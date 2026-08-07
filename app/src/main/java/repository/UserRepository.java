package repository;

import model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    boolean exists(Long id);
    void save(User user);
    void update(User user);
    void delete(Long id);
    List<User> getAllUser();
}
