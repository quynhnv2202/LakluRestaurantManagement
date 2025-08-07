package com.laklu.pos.dataObjects.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableInfo {
    Integer id;
    String tableNumber;
} 