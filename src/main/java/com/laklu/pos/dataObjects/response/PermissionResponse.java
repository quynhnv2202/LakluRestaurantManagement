package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PermissionResponse {
    private int id;
    private String alias;
    private String name;
    private String description;
}
