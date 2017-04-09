package service;

import model.User;

import java.util.Collection;
import java.util.Map;

public interface UserService {
    void join(Map<String, String> query);
    User login(String userId, String password);
    Collection<User> getAllUser();
}
