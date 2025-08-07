package com.laklu.pos.dataObjects.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ReservationTableUpdateDTO {
    private List<Integer> tableIds;
}
