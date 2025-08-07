package com.laklu.pos.dataObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PermissionGroupResponse {
    private String groupName;
    private String groupAlias;
    private String groupDescription;
    private List<PermissionResponse> permissions;
}
