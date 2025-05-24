package com.chave.bean;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import lombok.Data;
import java.util.ArrayList;

@Data
public class OriginRequestItem {
    private Integer id;
    private String host;
    private String method;
    private String path;
    private String responseLength;
    private String responseCode;
    private ArrayList<FuzzRequestItem> fuzzRequestArrayList;
    private HttpRequest originRequest;
    private HttpResponse originResponse;

    public OriginRequestItem(Integer id, String method, String host, String path, String responseLength, String responseCode) {
        this.id = id;
        this.host = host;
        this.method = method;
        this.path = path;
        this.responseLength = responseLength;
        this.responseCode = responseCode;
        this.fuzzRequestArrayList = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (((OriginRequestItem) o).getPath().equals(this.path) && ((OriginRequestItem) o).getHost().equals(this.host) && ((OriginRequestItem) o).getMethod().equals(this.method)) {
            return true;
        }

        return false;
    }
}
