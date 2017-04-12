package webserver;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class HttpResponseTest {



    private String testDirectory = "./src/test/resources/";

    private InputStream in;
    private BufferedReader br;
    private OutputStream out;
    private HttpResponse httpResponse;
    private String url = "/index.html";

    @Before
    public void setUp() throws FileNotFoundException, UnsupportedEncodingException {
        in = new FileInputStream(new File(testDirectory + "Http_Output.txt"));
        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        out = new FileOutputStream(new File(testDirectory + "Http_Output.txt"));
        httpResponse = new HttpResponse(out);
    }

    @Test
    public void responseForward() throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        httpResponse.forward(url);
        assertEquals("HTTP/1.1 200 OK ", br.readLine());
        assertEquals("Content-Type: text/html;charset=utf-8 ", br.readLine());
        assertEquals("Content-Length: " + body.length + " ", br.readLine());
        assertEquals("", br.readLine());
    }

    @Test
    public void responseSendRedirect() throws IOException {
        httpResponse.sendRedirect(url);
        assertEquals("HTTP/1.1 302 Found ", br.readLine());
        assertEquals("Content-Type: text/html ", br.readLine());
        assertEquals("Location: http://localhost:8080" + url + " ", br.readLine());
        assertEquals("", br.readLine());
    }

    @Test
    public void responseCookies() throws IOException {
        httpResponse.addCookie("logined=true");
        httpResponse.sendRedirect(url);
        assertEquals("HTTP/1.1 302 Found ", br.readLine());
        assertEquals("Content-Type: text/html ", br.readLine());
        assertEquals("Set-Cookie: logined=true ", br.readLine());
        assertEquals("Location: http://localhost:8080" + url + " ", br.readLine());
        assertEquals("", br.readLine());
    }
}