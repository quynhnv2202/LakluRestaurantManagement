package com.laklu.pos.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public enum Gender {
    MALE("Nam", "Giới tính nam"),
    FEMALE("Nữ", "Giới tính nữ"),
    OTHER("Khác", "Giới tính khác");

    Gender(String label, String description) {
        this.label = label;
        this.description = description;
    }

    String label;
    String description;

}
