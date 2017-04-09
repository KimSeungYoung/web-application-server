package service;

import model.User;

import java.util.Collection;
import java.util.Map;

public interface UserService {
    void join(Map<String, String> query);
    User login(Map<String, String> query);
    Collection<User> getAllUser();
}
