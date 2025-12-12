package common;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    public String type;
    public Object data;

    public Request(String type, Object data) {
        this.type = type;
        this.data = data;
    }
}