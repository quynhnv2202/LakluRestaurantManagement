package com.laklu.pos.dataObjects.request;

import com.laklu.pos.enums.SalaryType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class NewSalaryRate {
    private String levelName;
    private BigDecimal amount;
    private SalaryType type;
    private Boolean isGlobal;
}
