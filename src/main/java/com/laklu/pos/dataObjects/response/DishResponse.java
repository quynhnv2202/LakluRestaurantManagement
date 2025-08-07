package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Dish;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class DishResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Double price;
    private Boolean requiresPreparation;
    private List<PersistAttachmentResponse> images;

    public DishResponse(Integer id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt, Double price,Boolean requiresPreparation, List<PersistAttachmentResponse> images) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.price = price;
        this.requiresPreparation = requiresPreparation;
        this.images = images;
    }

    public static DishResponse fromEntity(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getCreatedAt() ,
                dish.getUpdatedAt(),
                dish.getPrice(),
                dish.getRequiresPreparation(),
                new ArrayList<>()
        );
    }
}
