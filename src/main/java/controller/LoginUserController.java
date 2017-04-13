package controller;

import com.google.common.collect.Maps;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import service.UserServiceImpl;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.isNull;

public class LoginUserController implements Controller {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private UserService userService = new UserServiceImpl();

    public static LoginUserController of() {
        return new LoginUserController();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        User user = userService.login(getQueryForParameter(request));
        responseLoginHeader(response, user);
    }

    private Map<String, String> getQueryForParameter(HttpRequest request) {
        Map<String, String> query = Maps.newHashMap();
        query.put("userId", request.getParameter("userId"));
        query.put("password", request.getParameter("password"));
        query.put("name", request.getParameter("name"));
        query.put("email", request.getParameter("email"));
        return query;
    }

    private void responseLoginHeader(HttpResponse response, User user) {
        if(isNull(user)) {
            responseLoginFailHeader(response);
        } else {
            responseLoginSuccessHeader(response);
        }
    }

    private void responseLoginFailHeader(HttpResponse response) {
        try {
            response.addHeader("Set-Cookie", "logined=false");
            response.sendRedirect("/user/login_failed.html");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLoginSuccessHeader(HttpResponse response) {
        try {
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/index.html");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
