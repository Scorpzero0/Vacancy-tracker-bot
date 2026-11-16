package repository;

import model.User;
import service.UserFileService;
import java.util.List;
import java.util.Optional;

public class JsonUserRepository implements UserRepository{

    UserFileService userFileService = new UserFileService();

    @Override
    public Optional<User> findById(Long id) {
        return userFileService.readAndGetUser(id);
    }

    @Override
    public boolean exists(Long id) {
        return userFileService.exists(id);
    }

    @Override
    public void save(User user) {
        userFileService.writeUserData(user);
    }

    @Override
    public void update(User user) {
        userFileService.updateUser(user);
    }

    @Override
    public void delete(Long id) {
        userFileService.deleteUserData(id);
    }

    @Override
    public List<User> getAllUser() {
        return userFileService.readAllUsers();
    }
}
