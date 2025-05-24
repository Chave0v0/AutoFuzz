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
        LOG.logToOutput("AutoFuzz v1.4");
        LOG.logToOutput("Author: Chave, z-bool, Y5neKO");
        LOG.logToOutput("Github: https://github.com/Chave0v0/AutoFuzz");

        // 加载配置文件
        if (YamlUtil.checkConfigDir()) {
            YamlUtil.loadYamlConfig();
        }


        // 初始化ui
        try {
            MainUI = new MainUI();
        } catch (Exception ex) {
            LOG.logToError("[ERROR] 初始化UI出现异常.");
        }

        API.userInterface().registerSuiteTab("AutoFuzz", MainUI.getMainSplitPane());
        API.http().registerHttpHandler(new AutoFuzzHandler());
        API.userInterface().registerContextMenuItemsProvider(new AutoFuzzMenu());
    }
}
