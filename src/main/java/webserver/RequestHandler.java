package webserver;

import com.google.common.collect.Maps;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import util.HttpRequestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import static java.util.Objects.isNull;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String DEFAULT_URL = "/index.html";

    private Socket connection;
    private UserService userService;

    public RequestHandler(Socket connectionSocket, UserService userService) {
        this.connection = connectionSocket;
        this.userService = userService;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = HttpRequest.of(in);
            HttpResponse response = HttpResponse.of(out);

            // url 파싱
            String url = request.getPath();

            log.debug(">> All users : {}", userService.getAllUser().toString());

            switch (url) {
                case "/user/create":
                    userService.join(getQueryForParameter(request));
                    response.sendRedirect("/index.html");
                    break;
                case "/user/login":
                    User user = userService.login(getQueryForParameter(request));
                    responseLoginHeader(response, user);
                    break;
                case "/user/list":
                    responseUserListHeader(response, getCookieMap(request));
                    break;
                case "":
                    response.forward(DEFAULT_URL);
                default:
                    response.forward(url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private Map<String, String> getCookieMap(HttpRequest request) {
        return HttpRequestUtils.parseCookies(request.getHeader("Cookie"));
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
        if(isNull(user)) responseLoginFailHeader(response);
        responseLoginSuccessHeader(response);
    }

    private void responseUserListHeader(HttpResponse response, Map<String, String> cookieMap) {
        if(cookieMap.get("logined").equals("true")) {
            responseListHeader(response);
        } else {
            responseLoginFailHeader(response);
        }
    }

    private void responseLoginSuccessHeader(HttpResponse response) {
        try {
            response.addCookie("logined=true");
            response.sendRedirect("/index.html");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLoginFailHeader(HttpResponse response) {
        try {
            response.addCookie("logined=false");
            response.sendRedirect("/user/login_failed.html");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseListHeader(HttpResponse response) {
        try {
            response.addCookie("logined=true");
            response.sendRedirect("/user/list.html");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
