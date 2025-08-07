package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NewRole {
    @NotNull
    private String name;
    private String description;
    private List<Integer> permissions = new ArrayList<>();
}
