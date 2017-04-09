package webserver;

import com.google.common.collect.Maps;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
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
import java.util.Map;

import static java.util.Objects.isNull;

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private UserService userService;

    public RequestHandler(Socket connectionSocket, UserService userService) {
        this.connection = connectionSocket;
        this.userService = userService;
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

            // index.html 응답하기 (url 파싱하기)
            String url = getUrl(line);

            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            log.debug(">> All users : {}", userService.getAllUser().toString());

            // POST 방식으로 회원가입하기
            if(url.equals("/user/create")) {
                Map<String, String> query = HttpRequestUtils.parseQueryString(getBodyString(br, line));
                userService.join(query);
                response302Header(dos);
            }

            // 로그인하기
            else if(url.equals("/user/login")) {
                Map<String, String> query = HttpRequestUtils.parseQueryString(getBodyString(br, line));
                User user = userService.login(query);
                responseLoginHeader(dos, user);
            }

            // 사용자 목록 출력
            else if(url.equals("/user/list")) {
                Map<String, String> cookieMap = getCookieMap(br, line);
                responseUserListHeader(dos, cookieMap);
            }

            else {
                while(!"".equals(line)) {
                    log.debug("header : {}", line);
                    line = br.readLine();
                }
            }

            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            responseDefaultHeader(dos, url, body);
            responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseDefaultHeader(DataOutputStream dos, String url, byte[] body) {
        // CSS 지원하기
        if(url.contains("/css/")) {
            responseCSSHeader(dos);
        } else {
            response200Header(dos, body.length);
        }
    }

    private Map<String, String> getCookieMap(BufferedReader br, String line) throws IOException {
        Map<String, String> cookieMap = Maps.newHashMap();
        while(!"".equals(line)) {
            if(line.contains("Cookie: ")) {
                cookieMap = HttpRequestUtils.parseCookies(line.split("Cookie: ")[1]);
            }
            line = br.readLine();
        }
        return cookieMap;
    }

    private String getUrl(String firstLine) {
        return firstLine.split(" ")[1];
    }

    private String getBodyString(BufferedReader br, String line) throws IOException {
        String bodyLength = getBodyLength(br, line);
        if(!bodyLength.equals("")) {
            return IOUtils.readData(br, Integer.parseInt(bodyLength));
        }
        throw new IOException();
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

    private void responseLoginHeader(DataOutputStream dos, User user) {
        if(isNull(user)) {
            responseLoginFailHeader(dos);
            log.debug(">> login false!!");
        } else {
            responseLoginSuccessHeader(dos);
            log.debug(">> login true!!");
        }
    }

    private void responseUserListHeader(DataOutputStream dos, Map<String, String> cookieMap) {
        if(cookieMap.get("logined").equals("true")) {
            responseListHeader(dos);
        } else {
            responseLoginFailHeader(dos);
        }
    }

    private void responseLoginSuccessHeader(DataOutputStream dos) {
        try {
            write302Header(dos);
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLoginFailHeader(DataOutputStream dos) {
        try {
            write302Header(dos);
            dos.writeBytes("Set-Cookie: logined=false \r\n");
            dos.writeBytes("Location: http://localhost:8080/user/login_failed.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseListHeader(DataOutputStream dos) {
        try {
            write302Header(dos);
            dos.writeBytes("Set-Cookie: logined=true \r\n");
            dos.writeBytes("Location: http://localhost:8080/user/list.html \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void write302Header(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 302 Found \r\n");
        dos.writeBytes("Content-Type: text/html \r\n");
    }

    private void responseCSSHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css,*/*;q=0.1 \r\n");
            dos.writeBytes("\r\n");
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

    // 302 status code 적용
    private void response302Header(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: http://localhost:8080/index.html \r\n");
            dos.writeBytes("\r\n");
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
