package com.ece416.aruproy.messengerclient;

/**
* Created by ilikecalculus on 2017-03-23.
*/

public enum MessageType{
    LIST_GROUP("0"), JOIN_GROUP("1"), NEW_MESSAGE("2"), LEAVE_GROUP("3");
    private String value;

    MessageType(String value) {
        this.value = value;
    }
}
