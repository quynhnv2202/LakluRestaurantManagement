package com.laklu.pos.mapper;


import com.laklu.pos.dataObjects.request.CategoryRequest;
import com.laklu.pos.dataObjects.response.CategoryResponse;
import com.laklu.pos.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {


    // Chuyển từ Request DTO -> Entity
    @Mapping(target = "id", ignore = true) // ID không cần map khi tạo mới
    @Mapping(target = "createdAt", ignore = true) // createdAt không map từ request
    @Mapping(target = "updatedAt", ignore = true) // updatedAt không map từ request
    @Mapping(target = "isDeleted", ignore = true) // Trạng thái xóa không map từ request
    Category toEntity(CategoryRequest request);

    // Chuyển từ Entity -> Response DTO
    CategoryResponse toResponse(Category category);

    // Chuyển danh sách Entity -> danh sách Response DTO
    List<CategoryResponse> toResponseList(List<Category> categories);
}
