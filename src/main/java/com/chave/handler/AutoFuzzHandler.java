package com.chave.handler;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import com.chave.Main;
import com.chave.config.UserConfig;
import com.chave.menu.AutoFuzzMenu;
import com.chave.bean.Data;
import com.chave.bean.OriginRequestItem;
import com.chave.service.AutoFuzzService;
import com.chave.utils.Util;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AutoFuzzHandler implements HttpHandler {
    private AutoFuzzService autoFuzzService = new AutoFuzzService();
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(200), new ThreadPoolExecutor.AbortPolicy());

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {

        try {
            // 插件需要启用
            if (UserConfig.TURN_ON) {
                String host = new URL(requestToBeSent.url()).getHost();
                // 先过滤域名
                if (Data.DOMAIN_LIST.size() > 0){
                    for (String domain : Data.DOMAIN_LIST) {
                        if(matchesDomain(host, domain, UserConfig.BLACK_OR_WHITE_CHOOSE)){
                            // 预检查
                            if (fuzzPreCheck(requestToBeSent, host)) {
                                autoFuzzService.preFuzz(requestToBeSent);
                            }
                            break;
                        }
                    }
                }else if(UserConfig.BLACK_OR_WHITE_CHOOSE){
                    if (fuzzPreCheck(requestToBeSent, host)) {
                        autoFuzzService.preFuzz(requestToBeSent);
                    }
                }
            }
        } catch (Exception e) {
            Main.LOG.logToError("request handler 出现异常" + e.getCause());
        }


        return null;
    }

    private boolean matchesDomain(String host, String domain, boolean isBlackOrWhite) {
        boolean isExactMatch = host.equals(domain);
        boolean isSubdomain = UserConfig.INCLUDE_SUBDOMAIN && host.endsWith(domain);

        return isBlackOrWhite
                ? (!isExactMatch && !isSubdomain) // 白名单模式：允许精确匹配或子域名
                : (isExactMatch || isSubdomain); // 黑名单模式：不允许域名或者子域名匹配到
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
                AutoFuzzMenu.ID--;

                autoFuzzService.startFuzz(AutoFuzzMenu.ID + 1);

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
            OriginRequestItem tempItem = new OriginRequestItem(null, requestToBeSent.method(), host, requestToBeSent.pathWithoutQuery(), null, null);
            if (item.equals(tempItem)) {
                return true;
            }
        }

        return false;
    }
}
