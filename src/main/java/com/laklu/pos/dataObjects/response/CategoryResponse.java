package com.laklu.pos.dataObjects.response;

import com.laklu.pos.entities.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse
{
    private Long id;
    private String name;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isDeleted;
    
    /**
     * Chuyển đổi từ entity sang response
     * @param category Entity cần chuyển đổi
     * @return CategoryResponse tương ứng
     */
    public static CategoryResponse fromEntity(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        
        // Chuyển đổi LocalDateTime sang Date nếu cần
        if (category.getCreatedAt() != null) {
            response.setCreatedAt(java.sql.Timestamp.valueOf(category.getCreatedAt()));
        }
        
        if (category.getUpdatedAt() != null) {
            response.setUpdatedAt(java.sql.Timestamp.valueOf(category.getUpdatedAt()));
        }
        
        response.setIsDeleted(category.getIsDeleted());
        return response;
    }
}
