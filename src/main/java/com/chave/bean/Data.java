package com.chave.bean;

import burp.api.montoya.http.message.requests.HttpRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Data {
    public static LinkedHashMap<Integer, OriginRequestItem> ORIGIN_REQUEST_TABLE_DATA = new LinkedHashMap<>();
    public static LinkedHashMap<Integer, ArrayList<HttpRequest>> NEW_REQUEST_TO_BE_SENT_DATA = new LinkedHashMap<>();
    public static ArrayList<String> DOMAIN_LIST = new ArrayList<>();
    public static ArrayList<String> PAYLOAD_LIST = new ArrayList<>();
    public static LinkedHashMap<String, String> HEADER_MAP = new LinkedHashMap<>();
}
