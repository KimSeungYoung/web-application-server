package webserver;

import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginUserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
                    CreateUserController.of().service(request, response);
                    break;
                case "/user/login":
                    LoginUserController.of().service(request, response);
                    break;
                case "/user/list":
                    ListUserController.of().service(request, response);
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
}
