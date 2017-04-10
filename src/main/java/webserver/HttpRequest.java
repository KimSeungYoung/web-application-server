package webserver;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private BufferedReader br;
    private String requestLine = "";
    private Map<String, String> headerMap = Maps.newHashMap();
    private Map<String, String> parameterMap = Maps.newHashMap();

    public HttpRequest(InputStream in) throws IOException {
        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = br.readLine();
        requestLine = line;

        log.debug("request line : {}", requestLine);

        if (requestLine == null) {
            return ;
        }

        while(!"".equals(line)) {
            line = br.readLine();
            log.debug("header : {}", line);
            if(!"".equals(line)) {
                headerMap.put(line.split(": ")[0], line.split(": ")[1]);
            }
        }
    }

    public String getMethod() throws IOException {
        return requestLine.split(" ")[0];
    }

    public String getPath() throws IOException {
        if(getMethod().equals("POST")) {
            return requestLine.split(" ")[1];
        } else {
            setParameter(requestLine.split(" ")[1].split("\\?")[1]);
            return requestLine.split(" ")[1].split("\\?")[0];
        }
    }

    public String getHeader(String key) {
        return headerMap.get(key);
    }

    public String getParameter(String userId) {
        return parameterMap.get(userId);
    }

    public void setParameter(String parameter) {
        parameterMap = HttpRequestUtils.parseQueryString(parameter);
    }
}
