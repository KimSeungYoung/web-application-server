package controller;

import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class ListUserController extends AbstractController {

    public static ListUserController of() {
        return new ListUserController();
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        responseUserListHeader(response, getCookieMap(request));
    }

    private Map<String, String> getCookieMap(HttpRequest request) {
        return HttpRequestUtils.parseCookies(request.getHeader("Cookie"));
    }

    private void responseUserListHeader(HttpResponse response, Map<String, String> cookieMap) {
        if(cookieMap.get("logined").equals("true")) {
            responseListHeader(response);
        } else {
            responseLoginFailHeader(response);
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

    private void responseListHeader(HttpResponse response) {
        try {
            response.addHeader("Set-Cookie", "logined=true");
            response.sendRedirect("/user/list.html");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
