package papka_HTTP;
import java.util.*;

public class HTTPRequest {
    private final AllMethods method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> parameters;
    private final byte[] body;

    public HTTPRequest(AllMethods vmethod, String vpath, Map<String, String> vheaders, Map<String, String> vparameters, byte[] vbody) {

        this.method = vmethod;
        this.path = vpath;
        this.headers = vheaders != null ? vheaders : new HashMap<>();
        this.parameters = vparameters != null ? vparameters : new HashMap<>();
        this.body = vbody != null ? vbody : new byte[0];
    }

    public AllMethods getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public byte[] getBody() {
        return body;
    }

}
