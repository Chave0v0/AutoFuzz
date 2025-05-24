package com.chave.utils;

import com.chave.Main;
import com.chave.bean.Data;
import com.chave.bean.OriginRequestItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

public class Util {

    // 刷新fuzz请求列表
    public static synchronized void addOriginRequestItem(OriginRequestItem item) {
        try {
            JTable originRequestItemTable = Main.MainUI.getOriginRequestItemTable();
            DefaultTableModel model = (DefaultTableModel) originRequestItemTable.getModel();

            model.addRow(new Object[]{item.getId(), item.getMethod(), item.getHost(), item.getPath(), item.getResponseLength(), item.getResponseCode()});

            // 始终以id排序
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) originRequestItemTable.getRowSorter();
            sorter.sort();
        } catch (NullPointerException nullPointerException) {
            // 不明原因出现空指针
        }

    }

    public static String fullyURLEncode(String input) throws UnsupportedEncodingException {
        StringBuilder encodedString = new StringBuilder();

        // Iterate over each character in the input string
        for (char ch : input.toCharArray()) {
            // Encode each character to its %XX format
            encodedString.append(String.format("%%%02X", (int) ch));
        }

        return encodedString.toString();
    }

    public synchronized static void setOriginRequestId(OriginRequestItem item) {
        item.setId(Data.ORIGIN_REQUEST_TABLE_DATA.size());
    }

    // 刷新domain表格
    public static synchronized void flushConfigTable(String type, JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);  // 清空表格

        switch (type) {
            case "domain":
                fillTableWithList(model, Data.DOMAIN_LIST);
                break;
            case "payload":
                fillTableWithList(model, Data.PAYLOAD_LIST);
                break;
            case "header":
                fillTableWithMap(model, Data.HEADER_MAP);
                break;
        }
    }

    private static void fillTableWithList(DefaultTableModel model, ArrayList<String> list) {
        ArrayList<String> tempList = new ArrayList<>();
        if (list.size() > 0) {
            for (String item : list) {
                if (!item.equals("")) {
                    tempList.add(item);
                }
                model.addRow(new Object[]{item});
            }
        }
    }

    private static void fillTableWithMap(DefaultTableModel model, LinkedHashMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }


    // 用户添加配置
    public static void addConfigData(String type, JTextArea userInputTextArea) {
        String userInput = userInputTextArea.getText();
        if (isBlankInput(userInput)) {
            return;
        }

        switch (type) {
            case "domain":
                addToDomainList(userInput);
                break;
            case "payload":
                addToPayloadList(userInput);
                break;
            case "header":
                addToHeaderMap(userInput);
                break;
        }
    }

    private static boolean isBlankInput(String input) {
        return input == null || input.trim().length() == 0;
    }

    private static void addToDomainList(String input) {
        processListInput(Data.DOMAIN_LIST, input);
    }

    private static void addToPayloadList(String input) {
        processListInput(Data.PAYLOAD_LIST, input);
    }

    private static void processListInput(ArrayList<String> targetList, String input) {
        String[] lines = input.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !targetList.contains(line)) {
                targetList.add(line);
            }
        }
    }

    private static void addToHeaderMap(String input) {
        LinkedHashMap<String, String> headerMap = Data.HEADER_MAP;
        String[] lines = input.split("\n");

        for (String line : lines) {
            String[] parts = line.split(":", 2);  // 最多分割成两部分
            if (parts.length == 2) {
                String header = parts[0].trim();
                String value = parts[1].trim();
                if (!header.isEmpty() && !value.isEmpty() && !headerMap.containsKey(header)) {
                    headerMap.put(header, value);
                }
            }
        }
    }
    // 用户编辑配置
    public static void editConfigData(String type, JTextField userInputTextField, int row) {
        String userInput = userInputTextField.getText();
        // 防止空指针
        if (userInput == null) {
            return;
        }

        // 去除前后空格换行符
        userInput = userInput.trim();

        // 防止只有空白字符
        if (userInput.length() == 0) {
            return;
        }


        if (type.equals("domain")) {
            ArrayList targetList = Data.DOMAIN_LIST;
            if (!targetList.contains(userInput)) {
                targetList.set(row, userInput);
            }
        } else if (type.equals("payload")) {
            ArrayList targetList = Data.PAYLOAD_LIST;
            if (!targetList.contains(userInput)) {
                targetList.set(row, userInput);
            }
        } else if (type.equals("header")) {
            LinkedHashMap<String, String> headerMap = Data.HEADER_MAP;

            int index = userInput.indexOf(":");
            if (index != -1) {  // 确保是合法的header的key-value
                String header = userInput.substring(0, index);  // 左边部分
                String value = userInput.substring(index + 1); // 右边部分（去掉冒号）
                if (headerMap.containsKey(header) && header != null && value != null) {
                    if (header.trim().length() != 0 && value.trim().length() != 0) {
                        headerMap.put(header.trim(), value.trim());
                    }
                }
            }
        }
    }

    // 用户删除配置
    public static void removeConfigData(String type, int[] rows) {
        if (rows.length == 0) {
            return;
        }

        switch (type) {
            case "domain":
                removeFromList(Data.DOMAIN_LIST, rows);
                break;
            case "payload":
                removeFromList(Data.PAYLOAD_LIST, rows);
                break;
            case "header":
                removeFromMap(Data.HEADER_MAP, rows);
                break;
        }
    }

    private static void removeFromList(ArrayList<String> list, int[] rows) {
        for (int i = rows.length - 1; i >= 0; i--) {
            list.remove(rows[i]);
        }
    }

    private static void removeFromMap(LinkedHashMap<String, String> map, int[] rows) {
        for (int i = rows.length - 1; i >= 0; i--) {
            int index = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (index == rows[i]) {
                    map.remove(entry.getKey());
                    break;
                }
                index++;
            }
        }
    }

    // 对有歧义的字符进行url编码
    public static String urlEncode(String input) throws UnsupportedEncodingException {
        StringBuilder encodedString = new StringBuilder();

        // 遍历字符串中的每个字符
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 如果是字母或数字，则不进行编码
            if (Character.isLetterOrDigit(c)) {
                encodedString.append(c);
            } else {
                // 对所有其他字符进行URL编码
                encodedString.append("%").append(String.format("%02X", (int) c)); // 其他字符进行URL编码
            }
        }

        return encodedString.toString();
    }


    public static Object isNumber(String value) {
        // 尝试将字符串转化为整数
        try {
            return Integer.parseInt(value);  // 如果能转化为整数，返回整数
        } catch (NumberFormatException e1) {
            // 如果转换为整数失败，尝试将其转化为 BigDecimal
            try {
                return new BigDecimal(value);  // 如果能转化为 BigDecimal，返回 BigDecimal
            } catch (NumberFormatException e2) {
                // 如果无法转换为整数或 BigDecimal，返回原始的字符串
                return value;
            }
        }
    }

    public static Object isBoolean(String value) {
        if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            return value;
        }
    }
}