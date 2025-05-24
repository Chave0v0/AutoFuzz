package com.chave.utils;

import com.chave.Main;
import com.chave.bean.Data;
import com.chave.bean.Language;
import com.chave.config.Config;
import com.chave.config.UserConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class YamlUtil {

    public static void loadYamlConfig() {
        try {
            File ruleYamlFile = new File(Config.CONFIG_FILE);
            if (!ruleYamlFile.exists()) {
                // 如果文件不存在 使用默认配置导出一份到文件
                exportToYaml();
                Main.LOG.logToOutput("[INFO] 成功创建默认配置文件: " + Config.CONFIG_FILE);
            } else {
                // 如果文件存在 加载配置文件
                FileInputStream fis = new FileInputStream(ruleYamlFile);
                LinkedHashMap map = new Yaml().load(fis);

                // 加载用户配置
                LinkedHashMap config = (LinkedHashMap) map.get("config");
                LinkedHashMap basicSetting = (LinkedHashMap) config.get("basic_setting");
                LinkedHashMap domainSetting = (LinkedHashMap) config.get("domain_setting");
                LinkedHashMap payloadSetting = (LinkedHashMap) config.get("payload_setting");
                LinkedHashMap authHeaderSetting = (LinkedHashMap) config.get("auth_header_setting");

                UserConfig.TURN_ON = (Boolean) basicSetting.get("turn_on");
                UserConfig.LISTEN_PROXY = (Boolean) basicSetting.get("listen_proxy");
                UserConfig.LISTEN_REPETER = (Boolean) basicSetting.get("listen_repeter");
                for (Language lang : Language.values()) {
                    if (basicSetting.get("language").equals(lang.name())) {
                        UserConfig.LANGUAGE = lang;
                        break;
                    }
                }
                UserConfig.BLACK_OR_WHITE_CHOOSE = (Boolean) domainSetting.get("black_or_white_choose");
                UserConfig.INCLUDE_SUBDOMAIN = (Boolean) domainSetting.get("include_subdomain");
                UserConfig.APPEND_MOD = (Boolean) payloadSetting.get("append_mod");
                UserConfig.PARAM_URL_ENCODE = (Boolean) payloadSetting.get("param_url_encode");
                UserConfig.UNAUTH = (Boolean) authHeaderSetting.get("unauth");


                // 加载payload
                ArrayList<LinkedHashMap> payload = (ArrayList<LinkedHashMap>) map.get("payload");
                if (payload != null) {
                    for (LinkedHashMap payloadmap : payload) {
                        Data.PAYLOAD_LIST.add((String) payloadmap.get("value"));
                    }
                }


                Main.LOG.logToOutput("[INFO] 已加载配置文件.");
            }

        } catch (Exception e) {
            Main.LOG.logToError("[ERROR] Yaml配置文件加载异常.");
        }
    }

    public static boolean checkConfigDir() {
        // 检查存放配置文件的目录是否存在，若不存在则创建目录
        File rule_dir = new File(Config.CONFIG_DIR);
        if (!rule_dir.exists()) {
            Main.LOG.logToOutput("[INFO] 配置文件目录不存在, 即将自动创建.");
            boolean created = rule_dir.mkdirs();  // 创建目录及其父目录
            if (created) {
                Main.LOG.logToOutput("[INFO] 配置文件目录创建成功: " + rule_dir.getAbsolutePath());
                return true;
            } else {
                Main.LOG.logToOutput("[ERROR] 配置文件目录创建失败.");
                return false;
            }
        } else {
            Main.LOG.logToOutput("[INFO] 配置文件目录已存在.");
            return true;
        }

    }


    public static void exportToYaml() {
        Map<String, Object> yamlData = new LinkedHashMap<>();

        LinkedHashMap<String, Object> configMap = new LinkedHashMap<>();
        LinkedHashMap<String, Object> basicSetting = new LinkedHashMap<>();
        LinkedHashMap<String, Object> domainSetting = new LinkedHashMap<>();
        LinkedHashMap<String, Object> payloadSetting = new LinkedHashMap<>();
        LinkedHashMap<String, Object> authHeaderSetting = new LinkedHashMap<>();
        basicSetting.put("turn_on", UserConfig.TURN_ON);
        basicSetting.put("listen_proxy", UserConfig.LISTEN_PROXY);
        basicSetting.put("listen_repeter", UserConfig.LISTEN_REPETER);
        basicSetting.put("language", UserConfig.LANGUAGE.name());
        domainSetting.put("black_or_white_choose", UserConfig.BLACK_OR_WHITE_CHOOSE);
        domainSetting.put("include_subdomain", UserConfig.INCLUDE_SUBDOMAIN);
        payloadSetting.put("append_mod", UserConfig.APPEND_MOD);
        payloadSetting.put("param_url_encode", UserConfig.PARAM_URL_ENCODE);
        authHeaderSetting.put("unauth", UserConfig.UNAUTH);
        configMap.put("basic_setting", basicSetting);
        configMap.put("domain_setting", domainSetting);
        configMap.put("payload_setting", payloadSetting);
        configMap.put("auth_header_setting", authHeaderSetting);

        ArrayList<LinkedHashMap<String, String>> payloadList = null;
        if (Data.PAYLOAD_LIST.size() > 0) {
            payloadList = new ArrayList<>();
            for (String payload : Data.PAYLOAD_LIST) {
                LinkedHashMap<String, String> payloadMap = new LinkedHashMap<>();
                payloadMap.put("value", payload);
                payloadList.add(payloadMap);
            }
        }


        yamlData.put("config", configMap);
        yamlData.put("payload", payloadList);

        // 配置YAML选项（保持块格式）
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);

        // 写入文件
        try {
            Yaml yaml = new Yaml(options);
            try (FileWriter writer = new FileWriter(Config.CONFIG_FILE)) {
                yaml.dump(yamlData, writer);
            }
        } catch (Exception e) {
            Main.LOG.logToError("[ERROR] 导出Yaml配置文件出现异常.");
        }


    }
}
