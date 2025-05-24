package com.chave.bean;

public enum Language {
    SIMPLIFIED_CHINESE("简体中文"),
    ENGLISH("English");

    private final String language;

    Language(String language)
    {
        this.language = language;
    }


    public String language()
    {
        return language;
    }


    @Override
    public String toString()
    {
        return language;
    }
}
