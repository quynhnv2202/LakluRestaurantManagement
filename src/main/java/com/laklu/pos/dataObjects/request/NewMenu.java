package com.laklu.pos.dataObjects.request;

import com.laklu.pos.entities.Menu;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewMenu {
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Nullable
    private Menu.MenuStatus status;
}
