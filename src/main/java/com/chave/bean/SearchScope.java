package com.chave.bean;

public enum SearchScope {
    REQUEST("request"),
    RESPONSE("response");

    private final String scopeName;

    SearchScope(String scopeName)
    {
        this.scopeName = scopeName;
    }


    public String scopeName()
    {
        return scopeName;
    }


    @Override
    public String toString()
    {
        return scopeName;
    }
}
