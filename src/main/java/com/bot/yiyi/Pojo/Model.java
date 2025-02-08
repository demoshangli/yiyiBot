package com.bot.yiyi.Pojo;

public enum Model {

    R1("deepseek-reasoner"),
    V3("deepseek-chat");

    private String model;

    Model(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }
}
