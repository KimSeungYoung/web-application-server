package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class TestControllerImpl implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        response.forward("/index.html");
    }
}
