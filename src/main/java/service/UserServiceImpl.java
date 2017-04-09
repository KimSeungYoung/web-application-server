package service;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    public void join(Map<String, String> query) {
        User user = new User(query.get("userId"), query.get("password"), query.get("name"), query.get("email"));
        DataBase.addUser(user);
        log.debug(">> Success join! user : {}", user);
    }

    @Override
    public User login(Map<String, String> query) {
        User user =  DataBase.findUserById(query.get("userId"));
        if (user.getPassword().equals(query.get("password"))) {
            return user;
        }
        else {
            return null;
        }
    }

    @Override
    public Collection<User> getAllUser() {
        return DataBase.findAll();
    }
}
