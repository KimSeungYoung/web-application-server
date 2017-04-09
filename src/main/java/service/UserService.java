package service;

import model.User;

public interface UserService {
    User join(String queryString);
    User login(String userId, String password);
}
