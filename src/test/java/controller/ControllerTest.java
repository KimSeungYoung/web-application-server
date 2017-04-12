package controller;

import org.junit.Before;
import org.junit.Test;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

public class ControllerTest {

    private String testDirectory = "./src/test/resources/";

    private InputStream in;
    private BufferedReader br;
    private OutputStream out;
    private HttpResponse response;

    @Before
    public void setUp() throws IOException {
        in = new FileInputStream(new File(testDirectory + "Http_Output.txt"));
        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        out = new FileOutputStream(new File(testDirectory + "Http_Output.txt"));
        response = new HttpResponse(out);
    }

    @Test
    public void createUserController() throws Exception {
        InputStream inputStream = new FileInputStream(new File(testDirectory + "Http_POST.txt"));
        HttpRequest request = new HttpRequest(inputStream);

        CreateUserController.of().service(request, response);

        assertEquals("HTTP/1.1 302 Found ", br.readLine());
        assertEquals("Content-Type: text/html;charset=utf-8 ", br.readLine());
        assertEquals("Location: http://localhost:8080/index.html ", br.readLine());
        assertEquals("", br.readLine());
    }

}