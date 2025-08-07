package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleResponse {
    private int id;
    private String name;
    private String description;
    private long userCount;
}
