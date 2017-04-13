package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class CreateUserController extends AbstractController implements Controller {

    public static CreateUserController of() {
        return new CreateUserController();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        userService.join(getQueryForParameter(request));
        response.sendRedirect("/index.html");
    }
}
