package com.chave.config;

public class Config {
    // 不进行fuzz的后缀
    public static final String[] STATIC_RESOURCE = new String[]{
            ".css", ".js", ".html", ".htm", ".xml", ".svg", ".woff", ".woff2",
            ".ttf", ".eot", ".otf", ".ico", ".jpg", ".jpeg", ".png", ".gif", ".bmp",
            ".tiff", ".webp", ".mp4", ".mp3", ".ogg", ".wav", ".flv", ".avi", ".mov",
            ".mkv", ".zip", ".tar", ".gzip", ".pdf", ".txt", ".csv", ".xls", ".xlsx",
            ".xml", ".yaml", ".md", ".rtf", ".ico", ".woff", ".woff2", ".eot", ".otf",
            ".ttf", ".svg", ".csv", "js.map"
    };

    // 配置文件路径
    public static String CONFIG_DIR =  System.getProperty("os.name").toLowerCase().contains("win") ? System.getProperty("user.home") + "\\.config\\AutoFuzz\\" : System.getProperty("user.home") + "/.config/AutoFuzz/";
    public static String CONFIG_FILE = System.getProperty("os.name").toLowerCase().contains("win") ? System.getProperty("user.home") + "\\.config\\AutoFuzz\\config.yml" : System.getProperty("user.home") + "/.config/AutoFuzz/config.yml";
}
