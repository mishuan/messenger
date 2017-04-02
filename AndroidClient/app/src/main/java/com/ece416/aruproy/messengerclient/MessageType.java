package com.ece416.aruproy.messengerclient;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* Created by ilikecalculus on 2017-03-23.
*/

public enum MessageType{
    LIST_GROUP("0"), JOIN_GROUP("1"), NEW_MESSAGE("2"), LEAVE_GROUP("3");
    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    private static final Map<String,MessageType> ENUM_MAP;

    // Build an immutable map of String name to enum pairs.
    // Any Map impl can be used.

    static {
        Map<String,MessageType> map = new ConcurrentHashMap<String,MessageType>();
        for (MessageType instance : MessageType.values()) {
            map.put(instance.getValue(),instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    public static MessageType get(String name) {
        return ENUM_MAP.get(name);
    }
}
