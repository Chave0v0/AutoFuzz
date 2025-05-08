package com.chave;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.logging.Logging;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chave.config.UserConfig;
import com.chave.handler.AutoFuzzHandler;
import com.chave.menu.AutoFuzzMenu;
import com.chave.pojo.Data;
import com.chave.pojo.FuzzRequestItem;
import com.chave.ui.MainUI;
import com.chave.utils.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main implements BurpExtension {
    public static MontoyaApi API;
    public static Logging LOG;
    public static MainUI MainUI;
    @Override
    public void initialize(MontoyaApi api) {
        // 初始化api与log
        API = api;
        LOG = api.logging();

        // banner info
        API.extension().setName("AutoFuzz");
        LOG.logToOutput("AutoFuzz v1.3");
        LOG.logToOutput("Author: Chave, z-bool, Y5neKO");
        LOG.logToOutput("Github: https://github.com/Chave0v0/AutoFuzz");

        // 初始化ui
        MainUI = new MainUI();
        API.userInterface().registerSuiteTab("AutoFuzz", MainUI.getMainSplitPane());
        API.http().registerHttpHandler(new AutoFuzzHandler());
        API.userInterface().registerContextMenuItemsProvider(new AutoFuzzMenu());
    }
}
