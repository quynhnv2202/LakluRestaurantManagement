package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class NewDish {
    @NotNull
    private String name;
    @NotNull
    private String description;

    @NotNull
    private List<Long> imageIds;

    @NotNull
    private Double price;

    @NotNull
    private Boolean requiresPreparation = false;
}
