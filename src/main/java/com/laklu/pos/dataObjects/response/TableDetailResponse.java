package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableDetailResponse {
    private int id;
    private String tableNumber;
    private int capacity;
    private String status;

    public TableDetailResponse(int id,String tableNumber, int capacity, String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }
}
