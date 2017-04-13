package controller;

import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

import static java.util.Objects.isNull;

public class LoginUserController extends AbstractController implements Controller {

    public static LoginUserController of() {
        return new LoginUserController();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        User user = userService.login(getQueryForParameter(request));
        responseLoginHeader(response, user);
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
