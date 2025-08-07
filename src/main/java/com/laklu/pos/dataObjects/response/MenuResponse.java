package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Menu;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuResponse {
    private Integer id;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Menu.MenuStatus status;
    private List<MenuItemResponse> menuItems;

    public MenuResponse(Integer id, String name, LocalDateTime startAt, LocalDateTime endAt, Menu.MenuStatus status) {
        this.id = id;
        this.name = name;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
    }

    public static MenuResponse fromEntity(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getName(), menu.getStartAt(), menu.getEndAt(), menu.getStatus());
    }
}
