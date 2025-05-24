package com.chave.config;

import com.chave.bean.Language;
import com.chave.bean.SearchScope;

public class UserConfig {
    public static Boolean TURN_ON = Boolean.TRUE;
    public static Boolean LISTEN_PROXY = Boolean.FALSE;
    public static Boolean LISTEN_REPETER = Boolean.FALSE;
    public static Language LANGUAGE = Language.SIMPLIFIED_CHINESE;

    public static Boolean BLACK_OR_WHITE_CHOOSE = Boolean.TRUE; // true为黑名单
    public static Boolean INCLUDE_SUBDOMAIN = Boolean.FALSE;

    public static Boolean APPEND_MOD = Boolean.FALSE;
    public static Boolean PARAM_URL_ENCODE = Boolean.FALSE;
    public static Boolean UNAUTH = Boolean.FALSE;

    public static SearchScope SEARCH_SCOPE;

}
