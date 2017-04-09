package service;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.Map;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    public User join(String queryString) {
        Map<String, String> query = HttpRequestUtils.parseQueryString(queryString);
        User user = new User(query.get("userId"), query.get("password"), query.get("name"), query.get("email"));
        log.debug("user : {}", user.toString());
        return null;
    }
}
