package webserver;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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
        if (requestLine == null) return ;

        setRequestLine(line);
        setHeaderMap(line);
        setParameter();
    }

    private void setRequestLine(String line) {
        this.requestLine = line;
    }

    private void setParameter() throws IOException {
        String parameter;
        if(getMethod().equals("POST") && !headerMap.get("Content-Length").equals("")) {
            parameter = IOUtils.readData(br, Integer.parseInt(headerMap.get("Content-Length")));
        } else {
            parameter = requestLine.split(" ")[1].split("\\?")[1];
        }
        parameterMap = HttpRequestUtils.parseQueryString(parameter);
    }

    private void setHeaderMap(String line) throws IOException {
        while(!"".equals(line)) {
            line = br.readLine();
            log.debug("header : {}", line);
            if(!"".equals(line)) {
                headerMap.put(line.split(":")[0], line.split(":")[1].trim());
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
            return requestLine.split(" ")[1].split("\\?")[0];
        }
    }

    public String getHeader(String key) {
        return headerMap.get(key);
    }

    public String getParameter(String userId) {
        return parameterMap.get(userId);
    }
}
