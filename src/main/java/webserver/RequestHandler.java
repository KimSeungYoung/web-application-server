package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import service.UserServiceImpl;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import static java.util.Objects.isNull;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private UserService userService = new UserServiceImpl();

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            log.debug("request line : {}", line);;
            DataOutputStream dos = new DataOutputStream(out);

            if (line == null) {
                return ;
            }

            // TODO 요구사항 1 - index.html 응답하기
            String url = line.split(" ")[1];
            log.debug("url : {}", url);

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            Collection<User> userList = DataBase.findAll();
            log.debug(">> All users : {}", userList.toString());


            // TODO 요구사항 2 - GET 방식으로 회원가입하기
            // TODO 요구사항 3 - POST 방식으로 회원가입하기
            if(url.contains("/user/create")) {
                joinUser(br, line);

                // TODO 요구사항 4 - 302 status cod 적용
                response302Header(dos);
            }
            // TODO 요구사항 5 - 로그인하기
            else if(line.contains("POST /user/login")) {

                User user = loginUser(br, line);
                log.debug(">> login : {}", user);

                if(isNull(user)) {
                    log.debug(">> login false!!");
                    responseLoginHeader(dos, false);
//                    url = "/user/login_failed.html";
                } else {
                    log.debug(">> login true!!");
                    responseLoginHeader(dos, true);
//                    url = "/index.html";
                }

            }
            else {
                while(!"".equals(line)) {
                    log.debug("header : {}", line);
                    line = br.readLine();
                }
            }

            log.debug(">> 200 OK!!");
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void joinUser(BufferedReader br, String line) throws IOException {
        String bodyLength = getBodyLength(br, line);
        if(!bodyLength.equals("")) {
            String bodyString = IOUtils.readData(br, Integer.parseInt(bodyLength));
            log.debug(">> body : {}", bodyString);
            userService.join(bodyString);
        }
    }

    private User loginUser(BufferedReader br, String line) throws IOException {
        String bodyLength = getBodyLength(br, line);
        if(!bodyLength.equals("")) {
            String bodyString = IOUtils.readData(br, Integer.parseInt(bodyLength));
            log.debug(">> body : {}", bodyString);
            Map<String, String> query = HttpRequestUtils.parseQueryString(bodyString);
            return userService.login(query.get("userId"), query.get("password"));
        }

        return null;
    }

    private String getBodyLength(BufferedReader br, String line) throws IOException {
        String bodyLength = "";
        while(!"".equals(line)) {
            log.debug("header : {}", line);
            if(line.contains("Content-Length: ")) {
                bodyLength = line.split("Content-Length: ")[1];
                log.debug("body length : {}", bodyLength);
            }
            line = br.readLine();
        }
        return bodyLength;
    }

    private void responseLoginHeader(DataOutputStream dos, Boolean isLogined) {
        log.debug(">> T/F : {}", String.valueOf(isLogined));
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Content-Type: text/html \r\n");
            dos.writeBytes("Set-Cookie: logined=" + String.valueOf(isLogined) + " \r\n");
            if(isLogined == true) {
                dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
            }
            else {
                dos.writeBytes("Location: http://localhost:8080/user/login_failed.html \r\n");
            }

            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
