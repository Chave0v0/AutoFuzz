package com.chave.handler;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import com.chave.Main;
import com.chave.config.UserConfig;
import com.chave.menu.AutoFuzzMenu;
import com.chave.pojo.Data;
import com.chave.pojo.OriginRequestItem;
import com.chave.service.AutoFuzzService;
import com.chave.utils.Util;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AutoFuzzHandler implements HttpHandler {
    private AutoFuzzService autoFuzzService = new AutoFuzzService();
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(200), new ThreadPoolExecutor.AbortPolicy());

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {

        try {
            // 插件需要启用
            if (UserConfig.TURN_ON) {
                // 先过滤域名
                String host = new URL(requestToBeSent.url()).getHost();
                for (String domain : Data.DOMAIN_LIST) {
                    if (host.equals(domain)) {
                        // 预检查
                        if (fuzzPreCheck(requestToBeSent, host)) {
                            autoFuzzService.preFuzz(requestToBeSent);
                        }
                        break;
                    } else if (UserConfig.INCLUDE_SUBDOMAIN && host.contains(domain)) {
                        // 预检查
                        if (fuzzPreCheck(requestToBeSent, host)) {
                            autoFuzzService.preFuzz(requestToBeSent);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Main.LOG.logToError("request handler 出现异常" + e.getCause());
        }


        return null;
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        int msgId = responseReceived.messageId();
        OriginRequestItem originRequestItem = Data.ORIGIN_REQUEST_TABLE_DATA.get(msgId);
        if (originRequestItem != null) {
            // 完成表格原请求返回包长度/状态码填写
            originRequestItem.setResponseLength(responseReceived.toString().length() + "");
            originRequestItem.setResponseCode(responseReceived.statusCode() + "");
            // 保存原请求响应数据
            originRequestItem.setOriginResponse(responseReceived);

            // 加入线程池进行fuzz
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    // 在子线程中执行 autoFuzzService.startFuzz(msgId);
                    autoFuzzService.startFuzz(msgId);
                }
            });

            // fuzz完成后向表格中添加originRequest条目
            Util.addOriginRequestItem(originRequestItem);
        } else {
            originRequestItem = Data.ORIGIN_REQUEST_TABLE_DATA.get(AutoFuzzMenu.ID);
            if (originRequestItem != null) {
                // 完成表格原请求返回包长度/状态码填写
                originRequestItem.setResponseLength(responseReceived.toString().length() + "");
                originRequestItem.setResponseCode(responseReceived.statusCode() + "");
                // 保存原请求响应数据
                originRequestItem.setOriginResponse(responseReceived);
                AutoFuzzMenu.ID--;

                // 加入线程池进行fuzz
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        // 在子线程中执行 autoFuzzService.startFuzz(msgId);
                        autoFuzzService.startFuzz(AutoFuzzMenu.ID + 1);
                    }
                });

                // fuzz完成后向表格中添加originRequest条目
                Util.addOriginRequestItem(originRequestItem);
            }
        }

        return null;
    }



    public static boolean fuzzPreCheck(HttpRequestToBeSent requestToBeSent, String host) {
        if ((UserConfig.LISTEN_PROXY && requestToBeSent.toolSource().isFromTool(ToolType.PROXY)) || (UserConfig.LISTEN_REPETER && requestToBeSent.toolSource().isFromTool(ToolType.REPEATER))) {
            // 监听捕获的请求进行去重
            if (!checkIsFuzzed(requestToBeSent, host)) {
                return true;
            }
        }

        return false;
    }

    private static boolean checkIsFuzzed(HttpRequestToBeSent requestToBeSent, String host) {
        for (Map.Entry<Integer, OriginRequestItem> entry : Data.ORIGIN_REQUEST_TABLE_DATA.entrySet()) {
            OriginRequestItem item = entry.getValue();
            OriginRequestItem tempItem = new OriginRequestItem(host, requestToBeSent.pathWithoutQuery(), null, null);
            if (item.equals(tempItem)) {
                return true;
            }
        }

        return false;
    }
}
