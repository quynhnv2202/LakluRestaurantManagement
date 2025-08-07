package com.laklu.pos.dataObjects.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.laklu.pos.enums.Department;

@Data
@Builder
public class NewUser {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String email;
    @NotNull
    private Department department;
    @NotNull
    private List<Integer> roleIds;
    @NotNull
    private Integer salaryRateId;
}