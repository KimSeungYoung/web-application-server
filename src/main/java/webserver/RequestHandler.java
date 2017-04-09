package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UserService;
import service.UserServiceImpl;
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

public class RequestHandler extends Thread {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

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

            // TODO 요구사항 3 - POST 방식으로 회원가입하기
            if(url.contains("/user/create")) {
                joinUser(br, line);

                // TODO 요구사항 4 - 302 status cod 적용
                dos = new DataOutputStream(out);
                response302Header(dos);
            } else {
                while(!"".equals(line)) {
                    log.debug("header : {}", line);
                    line = br.readLine();
                }
            }

            dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void joinUser(BufferedReader br, String line) throws IOException {
        String bodyLength = "";
        while(!"".equals(line)) {
            log.debug("header : {}", line);
            if(line.contains("Content-Length: ")) {
                bodyLength = line.split("Content-Length: ")[1];
                log.debug("body length : {}", bodyLength);
            }
            line = br.readLine();
        }

        if(!bodyLength.equals("")) {
            String bodyString = IOUtils.readData(br, Integer.parseInt(bodyLength));
            log.debug(">> body : {}", bodyString);

            UserService userService = new UserServiceImpl();
            userService.join(bodyString);
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
