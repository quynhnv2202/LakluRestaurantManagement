package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Table;
import lombok.Builder;
import lombok.Data;

@Data
public class TableResponse {
    private String tableNumber;
    private int capacity;
    private String status;

    public TableResponse(Table table) {
        this.tableNumber = table.getTableNumber();
        this.capacity = table.getCapacity();
        this.status = table.getStatus().toString();
    }
}
