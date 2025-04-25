package com.chave.menu;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import com.chave.Main;
import com.chave.service.AutoFuzzService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoFuzzMenu implements ContextMenuItemsProvider {
    private final AutoFuzzService autoFuzzService = new AutoFuzzService();
    public static Integer ID = -1;

    // 共享线程池
    private static final ExecutorService sharedExecutor = Executors.newCachedThreadPool(
            r -> {
                Thread t = new Thread(r, "AutoFuzz-Worker");
                t.setDaemon(true);
                return t;
            }
    );

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        ArrayList<Component> menus = new ArrayList<>();
        JMenuItem sentToAutoFuzzMenuItem = new JMenuItem("Send to AutoFuzz");

        HttpRequestResponse httpRequestResponse = event.messageEditorRequestResponse().get().requestResponse();
        HttpRequest request = httpRequestResponse.request();

        sentToAutoFuzzMenuItem.addActionListener(e -> {
            try {
                autoFuzzService.preFuzz(request);
                sharedExecutor.submit(() -> Main.API.http().sendRequest(request));
            } catch (Exception exception) {
                Main.LOG.logToError("右键主动fuzz出现异常" + exception.getCause());
            }
        });

        menus.add(sentToAutoFuzzMenuItem);
        return menus;
    }
}