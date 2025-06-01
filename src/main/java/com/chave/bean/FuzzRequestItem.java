package com.chave.bean;

import burp.api.montoya.http.message.HttpRequestResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FuzzRequestItem {
    private String param;
    private String payload;
    private String responseLength;
    private String responseLengthChange;
    private String responseCode;
    private String responseTime;
    private OriginRequestItem originRequestItem;
    private HttpRequestResponse fuzzRequestResponse;

    public FuzzRequestItem(String param, String payload, String responseLength, String responseLengthChange, String responseCode, String responseTime, OriginRequestItem originRequestItem) {
        this.param = param;
        this.payload = payload;
        this.responseLength = responseLength;
        this.responseLengthChange = responseLengthChange;
        this.responseCode = responseCode;
        this.responseTime = responseTime;
        this.originRequestItem = originRequestItem;
    }
}
