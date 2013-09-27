package models;

import com.avaje.ebean.annotation.EnumValue;

public enum MessageType {
        @EnumValue("D")
        Default,

        @EnumValue("A")
        Ausleihe,

        @EnumValue("N")
        Notice,


}
