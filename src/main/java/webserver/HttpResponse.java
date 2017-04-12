package webserver;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

public class HttpResponse {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private DataOutputStream dos;

    private Map<String, String> headerMap = Maps.newHashMap();
    private Map<String, String> addedHeaderMap = Maps.newHashMap();

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
        setHeaderMap();
    }

    public static HttpResponse of(OutputStream out) {
        return new HttpResponse(out);
    }

    private void setHeaderMap() {
        headerMap.put("200-OK", "HTTP/1.1 200 OK \r\n");
        headerMap.put("302-Found", "HTTP/1.1 302 Found \r\n");
        headerMap.put("Content-Type-Html", "Content-Type: text/html;charset=utf-8 \r\n");
        headerMap.put("Content-Type-Css", "Content-Type: text/css,*/*;q=0.1 \r\n");
        headerMap.put("Location-Default", "Location: http://localhost:8080");
        headerMap.put("Set-Cookie-True", "Set-Cookie: logined=true \r\n");
        headerMap.put("Set-Cookie-False", "Set-Cookie: logined=false \r\n");
    }

    public void forward(String url) throws IOException {
        response200Header(url, getBody(url));
        responseBody(getBody(url));
    }

    public void sendRedirect(String url) throws IOException {
        response302Header(url);
    }

    public void addHeader(String key, String value) {
        addedHeaderMap.put(key, key + ": " + value + " \r\n");
    }

    private byte[] getBody(String url) throws IOException {
        return Files.readAllBytes(new File("./webapp" + url).toPath());
    }

    private String getContentTypeHeader(String url) {
        return url.contains("/css/") ? headerMap.get("Content-Type-Css") : headerMap.get("Content-Type-Html");
    }

    private void response302Header(String url) {
        try {
            dos.writeBytes(headerMap.get("302-Found"));
            dos.writeBytes(headerMap.get("Content-Type-Html"));
            writeAddedHeader();
            dos.writeBytes(getLocationHeader(url));
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeAddedHeader() throws IOException {
        // 방법 1
        for(Map.Entry<String, String> elem : addedHeaderMap.entrySet()) {
            dos.writeBytes(elem.getValue());
        }
/*
        // 방법 2
        Iterator<String> iterator = addedHeaderMap.keySet().iterator();
        while(iterator.hasNext()) {
            dos.writeBytes(addedHeaderMap.get(iterator.next()));
        }
*/
    }

    private String getLocationHeader(String url) {
        return headerMap.get("Location-Default") + url + " \r\n";
    }

    private void response200Header(String url, byte[] body) {
        try {
            dos.writeBytes(headerMap.get("200-OK"));
            dos.writeBytes(getContentTypeHeader(url));
            dos.writeBytes(getContentLengthHeader(body.length));
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private String getContentLengthHeader(int bodyLength) {
        return "Content-Length: " + bodyLength + " \r\n";
    }

    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
