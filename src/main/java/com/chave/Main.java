package com.chave;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.logging.Logging;
import com.chave.handler.AutoFuzzHandler;
import com.chave.menu.AutoFuzzMenu;
import com.chave.ui.MainUI;
import com.chave.utils.YamlUtil;

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
        LOG.logToOutput("AutoFuzz v1.5");
        LOG.logToOutput("Author: Chave");
        LOG.logToOutput("Contributors: z-bool, Y5neKO, d1sbb");
        LOG.logToOutput("Github: https://github.com/Chave0v0/AutoFuzz");

        // 加载配置文件
        if (YamlUtil.checkConfigDir()) {
            YamlUtil.loadYamlConfig();
        }


        // 初始化ui
        MainUI = new MainUI();

        API.userInterface().registerSuiteTab("AutoFuzz", MainUI.getMainSplitPane());
        API.http().registerHttpHandler(new AutoFuzzHandler());
        API.userInterface().registerContextMenuItemsProvider(new AutoFuzzMenu());
    }
}
