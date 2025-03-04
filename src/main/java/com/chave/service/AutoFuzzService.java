package com.chave.service;

import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.params.ParsedHttpParameter;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.chave.Main;
import com.chave.config.Config;
import com.chave.config.UserConfig;
import com.chave.menu.AutoFuzzMenu;
import com.chave.pojo.Data;
import com.chave.pojo.FuzzRequestItem;
import com.chave.pojo.OriginRequestItem;
import com.chave.utils.Util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class AutoFuzzService {

    // 做一些准备工作  解析参数  准备待发送请求列表  初始化表格数据
    public synchronized void preFuzz(HttpRequest request) throws UnsupportedEncodingException, MalformedURLException {
        // 如果没有参数 直接返回
        if (!request.hasParameters()) {
            return;
        }

        // 如果是静态资源 直接返回
        for (String suffix : Config.STATIC_RESOURCE) {
            if (request.pathWithoutQuery().endsWith(suffix)) {
                return;
            }
        }


        // 创建表格数据
        OriginRequestItem originRequestItem = new OriginRequestItem(Data.ORIGIN_REQUEST_TABLE_DATA.size() + 1, request.method(), new URL(request.url()).getHost(), request.pathWithoutQuery(), null, null);
        // 创建newRequestToBeSent列表
        ArrayList<HttpRequest> newRequestToBeSentList = new ArrayList<>();
        // 保存原请求数据
        originRequestItem.setOriginRequest(request);
        if (request instanceof HttpRequestToBeSent) {  // 如果来源是监听，则存入msgID
            HttpRequestToBeSent requestToBeSent = (HttpRequestToBeSent) request;
            Data.ORIGIN_REQUEST_TABLE_DATA.put(requestToBeSent.messageId(), originRequestItem);
            Data.NEW_REQUEST_TO_BE_SENT_DATA.put(requestToBeSent.messageId(), newRequestToBeSentList);
        } else {  // 如果来源是插件主动扫描 则存入静态变量递减做id
            Data.ORIGIN_REQUEST_TABLE_DATA.put(AutoFuzzMenu.ID, originRequestItem);
            Data.NEW_REQUEST_TO_BE_SENT_DATA.put(AutoFuzzMenu.ID, newRequestToBeSentList);
        }



        // 处理没有json参数的情况
        if (!request.hasParameters(HttpParameterType.JSON)) {
            // 如果设置了header 自动开始检查
            if (Data.HEADER_MAP.size() != 0) {
                if (UserConfig.UNAUTH) {  // 如果开启未授权检查 则去除对应header
                    HttpRequest newRequest = request;
                    for (Map.Entry<String, String> entry : Data.HEADER_MAP.entrySet()) {  // 去除所有设置的header
                        newRequest = newRequest.withRemovedHeader(entry.getKey());
                    }
                    newRequestToBeSentList.add(newRequest);
                    originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem("*HEADER*", "*unauth*", null, null, originRequestItem));
                }

                HttpRequest newRequest = request;
                for (Map.Entry<String, String> entry : Data.HEADER_MAP.entrySet()) {  // 替换掉所有header
                    newRequest = newRequest.withHeader(entry.getKey(), entry.getValue());
                }
                newRequestToBeSentList.add(newRequest);
                originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem("*HEADER*", "*auth*", null, null, originRequestItem));
            }

            // 获取所有请求参数
            List<ParsedHttpParameter> parameters = request.parameters();
            for (ParsedHttpParameter parameter : parameters) {
                // 这里不考虑cookie，multipart，xml
                if (parameter.type().equals(HttpParameterType.COOKIE) || parameter.type().equals(HttpParameterType.MULTIPART_ATTRIBUTE) || parameter.type().equals(HttpParameterType.XML) || parameter.type().equals(HttpParameterType.XML_ATTRIBUTE)) {
                    continue;
                }
                for (String payload : Data.PAYLOAD_LIST) {
                    // 创建新请求
                    HttpRequest newRequest;
                    if (UserConfig.PARAM_URL_ENCODE) {  // 检查urlencode是否开启
                        newRequest = request.withParameter(HttpParameter.parameter(parameter.name(), Util.urlEncode(payload), parameter.type()));
                    } else {
                        newRequest = request.withParameter(HttpParameter.parameter(parameter.name(), payload, parameter.type()));
                    }

                    newRequestToBeSentList.add(newRequest);  // 添加待发送请求
                    originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem(parameter.name(), payload, null, null, originRequestItem));  // 添加表格数据
                }
            }

        } else {  // 处理请求中有json参数的情况
            // 如果设置了header 自动开始检查
            if (Data.HEADER_MAP.size() != 0) {
                if (UserConfig.UNAUTH) {  // 如果开启未授权检查 则去除对应header
                    HttpRequest newRequest = request;
                    for (Map.Entry<String, String> entry : Data.HEADER_MAP.entrySet()) {  // 去除所有设置的header
                        newRequest = newRequest.withRemovedHeader(entry.getKey());
                    }
                    newRequestToBeSentList.add(newRequest);
                    originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem("*HEADER*", "*unauth*", null, null, originRequestItem));
                }

                HttpRequest newRequest = request;
                for (Map.Entry<String, String> entry : Data.HEADER_MAP.entrySet()) {  // 替换掉所有header
                    newRequest = newRequest.withHeader(entry.getKey(), entry.getValue());
                }
                newRequestToBeSentList.add(newRequest);
                originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem("*HEADER*", "*auth*", null, null, originRequestItem));
            }

            // 先获取普通参数
            List<ParsedHttpParameter> parameters = new ArrayList<>();
            for (ParsedHttpParameter parameter : request.parameters()) {
                if (parameter.type().equals(HttpParameterType.URL) || parameter.type().equals(HttpParameterType.BODY)) {
                    parameters.add(parameter);
                }
            }
            // 先将普通参数的新请求添加完成
            for (ParsedHttpParameter parameter : parameters) {
                for (String payload : Data.PAYLOAD_LIST) {
                    HttpRequest newRequest;
                    if (UserConfig.PARAM_URL_ENCODE) {  // 检查urlencode是否开启
                        newRequest = request.withParameter(HttpParameter.parameter(parameter.name(), Util.urlEncode(payload), parameter.type()));
                    } else {
                        newRequest = request.withParameter(HttpParameter.parameter(parameter.name(), payload, parameter.type()));
                    }

                    newRequestToBeSentList.add(newRequest);  // 添加待发送请求
                    originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem(parameter.name(), payload, null, null, originRequestItem));  // 添加表格数据
                }
            }

            // 获取json字符串
            String json = request.body().toString();

            // 这里开始处理json
            JSONObject jsonObject = null;
            try {
                jsonObject = JSON.parseObject(json);
            } catch (JSONException jsonException) {
                jsonObject = JSON.parseObject("{}");  // 如果解析异常就替换为一个空的json串
                Main.LOG.logToError("json解析异常" + jsonException.getCause());
            }

            // jsonObject 解析成功
            if (jsonObject.size() != 0) {
                LinkedHashMap<HashMap<Integer, Object>, HashMap<String, Object>> result = new LinkedHashMap<>();  // 用于存放要替换的value
                parseJsonParam(null, jsonObject, result);  // 解析json获取所有要替换的value


                for (Map.Entry<HashMap<Integer, Object>, HashMap<String, Object>> resultEntry : result.entrySet()) {
                    HashMap<Integer, Object> resultKey = resultEntry.getKey();

                    for (Integer integer : resultEntry.getKey().keySet()) {
                        for (String payload : Data.PAYLOAD_LIST) {
                            jsonObject = JSON.parseObject(json);
                            String newJsonBody = updateJsonValue(integer, payload, null, jsonObject, result).get("json").toString();  // 生成新的payload

                            HttpRequest newRequest = request.withBody(newJsonBody);
                            newRequestToBeSentList.add(newRequest);  // 添加到待发送请求
                            originRequestItem.getFuzzRequestArrayList().add(new FuzzRequestItem((String) resultKey.get(integer), payload, null, null, originRequestItem));  // 添加表格数据
                        }
                    }

                }
            }
        }
    }


    // 发送请求
    public void startFuzz(int msgId) {
        // 如果记录中没有的 直接return掉
        if (!Data.NEW_REQUEST_TO_BE_SENT_DATA.containsKey(msgId)) {
            return;
        }
        ArrayList<HttpRequest> requestToBeSentList = Data.NEW_REQUEST_TO_BE_SENT_DATA.get(msgId);
        ArrayList<FuzzRequestItem> fuzzRequestItemArrayList = Data.ORIGIN_REQUEST_TABLE_DATA.get(msgId).getFuzzRequestArrayList();

        int i = 0;
        for (HttpRequest request : requestToBeSentList) {
            HttpRequestResponse httpRequestResponse = Main.API.http().sendRequest(request);
            FuzzRequestItem fuzzRequestItem = fuzzRequestItemArrayList.get(i);
            fuzzRequestItem.setFuzzRequestResponse(httpRequestResponse);  // 与table数据关联

            // 获取返回包长度变化
            int lengthChange = httpRequestResponse.response().toString().length() - Integer.parseInt(fuzzRequestItem.getOriginRequestItem().getResponseLength());
            fuzzRequestItem.setResponseLengthChange((lengthChange > 0 ? "+" + lengthChange : String.valueOf(lengthChange)));
            fuzzRequestItem.setResponseCode(httpRequestResponse.response().statusCode() + "");

            i++;
        }

        Data.NEW_REQUEST_TO_BE_SENT_DATA.remove(msgId);

    }

    // 用于解析json中所有需要fuzz的value
    public void parseJsonParam(Object jsonKey, Object jsonObj, LinkedHashMap result) {
        // 递归处理不同的 JSON 类型
        if (jsonObj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) jsonObj;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                Object value = entry.getValue();
                Object key = entry.getKey();

                parseJsonParam(key, value, result);
            }
        } else if (jsonObj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonObj;
            for (Object value : jsonArray) {
                parseJsonParam(jsonKey, value, result);
            }
        } else {
            // 每次遇到一个基本类型的值，保存它的类型和具体值
            HashMap<String, Object> valueMap = new HashMap<>();
            HashMap<Integer, Object> keyMap = new HashMap<>();
            if (jsonObj instanceof String) {
                valueMap.put("String", jsonObj);
            } else if (jsonObj instanceof BigDecimal) {
                valueMap.put("BigDecimal", jsonObj);
            } else if (jsonObj instanceof Integer) {
                valueMap.put("Integer", jsonObj);
            }
            keyMap.put(result.size() + 1, jsonKey);

            result.put(keyMap, valueMap);
        }
    }

    public HashMap updateJsonValue(int i, String payload, Object jsonKey, Object jsonObj, LinkedHashMap<HashMap<Integer, Object>, HashMap<String, Object>> result) {
        HashMap newJsonStringMap = new HashMap();
        newJsonStringMap.put("isModified", false);
        newJsonStringMap.put("json", jsonObj);
        // 递归处理不同的 JSON 类型
        if (jsonObj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) jsonObj;
            // 传进来的是json 遍历每个entry
            for (Map.Entry<String, Object> entry : ((JSONObject) jsonObj).entrySet()) {
                Object value = entry.getValue();
                Object key = entry.getKey();
                // 如果entry是基础类型 则开始替换payload
                if (!(value instanceof JSONObject) && !(value instanceof JSONArray)) {
                    Object resultValue = null;
                    Object resultValueType = null;

                    for (Map.Entry<HashMap<Integer, Object>, HashMap<String, Object>> resultEntry : result.entrySet()) {
                        for (Map.Entry<Integer, Object> keyEntry : resultEntry.getKey().entrySet()) {
                            if (keyEntry.getKey().equals(i)) {
                                for (Map.Entry<String, Object> valueEntry : resultEntry.getValue().entrySet()) {
                                    resultValue = valueEntry.getValue();
                                    resultValueType = valueEntry.getKey();
                                }
                            }
                        }
                    }

                    // 如果与resultValue相同 那么就是需要修改的值
                    if (resultValue.equals(value)) {
                        if (resultValueType.equals("BigDecimal") || resultValueType.equals("Integer")) {
                            jsonObject.put(entry.getKey(), Util.isNumber(payload));
                        } else {
                            jsonObject.put(entry.getKey(), payload);
                        }

                        newJsonStringMap.put("isModified", true);
                        newJsonStringMap.put("json", jsonObject);
                        return newJsonStringMap;
                    }
                } else {
                    // 如果依然是json嵌套 那么递归调用
                    HashMap tmpResult = updateJsonValue(i, payload, key, value, result);
                    jsonObject.put(entry.getKey(), tmpResult.get("json"));
                    if ((boolean) tmpResult.get("isModified")) {
                        newJsonStringMap.put("json", jsonObject);
                        return newJsonStringMap;
                    }
                }
            }
        } else if (jsonObj instanceof JSONArray) {  // 用于处理jsonArray类型
            JSONArray jsonArray = (JSONArray) jsonObj;

            for (int index = 0; index < jsonArray.size(); index++) {
                Object value = jsonArray.get(index);
                // 如果数组中这个元素是基本类型 开始准备替换
                if (!(value instanceof JSONObject) && !(value instanceof JSONArray)) {
                    Object resultValue = null;
                    Object resultValueType = null;

                    for (Map.Entry<HashMap<Integer, Object>, HashMap<String, Object>> resultEntry : result.entrySet()) {
                        for (Map.Entry<Integer, Object> keyEntry : resultEntry.getKey().entrySet()) {
                            if (keyEntry.getKey().equals(i)) {
                                for (Map.Entry<String, Object> valueEntry : resultEntry.getValue().entrySet()) {
                                    resultValue = valueEntry.getValue();
                                    resultValueType = valueEntry.getKey();
                                }
                            }
                        }
                    }

                    // 如果与resultValue相同 那么就是需要修改的值
                    if (resultValue.equals(value)) {
                        if (resultValueType.equals("BigDecimal") || resultValueType.equals("Integer")) {
                            jsonArray.set(index, Util.isNumber(payload));
                        } else {
                            jsonArray.set(index, payload);
                        }

                        newJsonStringMap.put("isModified", true);
                        newJsonStringMap.put("json", jsonArray);
                        return newJsonStringMap;
                    }
                } else {
                    // 如果依然是json嵌套 那么递归调用
                    HashMap tmpResult = updateJsonValue(i, payload, jsonKey, value, result);
                    jsonArray.set(index, tmpResult.get("json"));
                    if ((boolean) tmpResult.get("isModified")) {
                        newJsonStringMap.put("json", jsonArray);
                        return newJsonStringMap;
                    }
                }
            }

        }

        // 用于嵌套的jsonObject类型没有做任何修改时 返回原jsonObject
        return newJsonStringMap;
    }
}
