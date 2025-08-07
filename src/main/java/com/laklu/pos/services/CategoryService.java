package com.laklu.pos.services;

import com.laklu.pos.dataObjects.request.CategoryRequest;
import com.laklu.pos.entities.Category;
import com.laklu.pos.entities.Menu;
import com.laklu.pos.entities.MenuItem;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.mapper.CategoryMapper;
import com.laklu.pos.repositories.CategoryRepository;
import com.laklu.pos.repositories.MenuItemRepository;
import com.laklu.pos.validator.RuleValidator;
import com.laklu.pos.validator.ValueExistIn;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final MenuItemRepository menuItemRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getAllCategoriesDeleted() {
        return categoryRepository.findAllByIsDeletedFalse();
    }

    public Category createCategory(CategoryRequest categoryRequest) {
        ValueExistIn<String> valueExistIn = new ValueExistIn<>(
                "name",
                categoryRequest.getName(),
                (name) -> categoryRepository.findByName(name).isEmpty()
        );
        valueExistIn.setMessage("Danh mục đã tồn tại");
        RuleValidator.validate(valueExistIn);

        Category category = categoryMapper.toEntity(categoryRequest);
        category.setIsDeleted(false);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Category category, CategoryRequest categoryRequest) {
        ValueExistIn<String> valueExistIn = new ValueExistIn<>(
                "name",
                categoryRequest.getName(),
                (name) -> categoryRepository.findByName(name).isEmpty()
        );
        valueExistIn.setMessage("Danh mục đã tồn tại");
        RuleValidator.validate(valueExistIn);

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        return category;
    }

    @Transactional
    public void deleteCategory(Category category) {
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }


    public Category findOrFail(Long id) {
        return this.categoryRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    @Transactional
    public Category updateCategoryPartially(Category category, Map<String, Object> updates) {
        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> category.setName((String) value);
                case "description" -> category.setDescription((String) value);
            }
        });

        return categoryRepository.save(category);
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public boolean isCategoryInUse(Category category) {
        // Kiểm tra xem có bất kỳ MenuItem nào đang sử dụng category này không
        return menuItemRepository.existsByCategory(category);
    }
}
