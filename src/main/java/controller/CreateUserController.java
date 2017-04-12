package controller;

import com.google.common.collect.Maps;
import service.UserService;
import service.UserServiceImpl;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class CreateUserController implements Controller {

    private UserService userService = new UserServiceImpl();

    public static CreateUserController of() {
        return new CreateUserController();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        userService.join(getQueryForParameter(request));
        response.sendRedirect("/index.html");
    }

    private Map<String, String> getQueryForParameter(HttpRequest request) {
        Map<String, String> query = Maps.newHashMap();
        query.put("userId", request.getParameter("userId"));
        query.put("password", request.getParameter("password"));
        query.put("name", request.getParameter("name"));
        query.put("email", request.getParameter("email"));
        return query;
    }
}
