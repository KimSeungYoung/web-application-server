package controller;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import service.UserServiceImpl;
import webserver.HttpRequest;

import java.util.Map;

public abstract class AbstractController implements Controller {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected UserService userService = new UserServiceImpl();

    protected Map<String, String> getQueryForParameter(HttpRequest request) {
        Map<String, String> query = Maps.newHashMap();
        query.put("userId", request.getParameter("userId"));
        query.put("password", request.getParameter("password"));
        query.put("name", request.getParameter("name"));
        query.put("email", request.getParameter("email"));
        return query;
    }
}
