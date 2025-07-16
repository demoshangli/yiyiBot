package com.bot.yiyi.config;


import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class NumericBooleanDeserializer implements ObjectDeserializer {

    @Override
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object o) {
        String value = defaultJSONParser.parseObject(String.class);
        return (T) Boolean.valueOf("1".equals(value));
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}