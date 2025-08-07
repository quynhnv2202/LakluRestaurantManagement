package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferTableRequest {
    @NotNull(message = "Danh sách bàn cần chuyển không được để trống")
    private List<Integer> fromTableIds;
    
    @NotNull(message = "Danh sách bàn chuyển đến không được để trống")
    private List<Integer> toTableIds;
} 