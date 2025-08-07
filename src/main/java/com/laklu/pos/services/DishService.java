package com.laklu.pos.services;

import com.laklu.pos.dataObjects.response.PersistAttachmentResponse;
import com.laklu.pos.entities.Attachment;
import com.laklu.pos.entities.Category;
import com.laklu.pos.entities.Dish;
import com.laklu.pos.exceptions.httpExceptions.NotFoundException;
import com.laklu.pos.repositories.DishRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DishService {

    private final DishRepository dishRepository;

    public List<Dish> getAll() {
        return dishRepository.findAll();
    }
    
    public Page<Dish> getAllWithPagination(Pageable pageable) {
        return dishRepository.findAll(pageable);
    }

    public Optional<Dish> findById(Integer id) {
        return dishRepository.findById(id);
    }

    public Dish createDish(Dish dish) {
        return dishRepository.save(dish);
    }

    public Dish updateDish(Dish dish) {
        return dishRepository.save(dish);
    }

    public Dish findOrFail(Integer id) {
        return findById(id)
                .orElseThrow(NotFoundException::new);
    }

    public void deleteDish(Dish dish) {
        dishRepository.delete(dish);
    }

    public Optional<Dish> findByName(String name) {
        return dishRepository.findByName(name);
    }
    
    public List<Dish> searchByName(String name) {
        return dishRepository.findByNameContainingIgnoreCase(name);
    }
    
    public Page<Dish> searchByNameWithPagination(String name, Pageable pageable) {
        return dishRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Set<PersistAttachmentResponse> getDishImages(Integer dishId) {
        // Lấy món ăn từ dishId
        Dish dish = this.findOrFail(dishId);

        // Lấy Set các attachment từ món ăn
        Set<Attachment> attachments = dish.getAttachments();

        // Chuyển đổi Set<Attachment> thành Set<PersistAttachmentResponse>
        return attachments.stream()
                .map(a -> new PersistAttachmentResponse(a.getId(), a.getPath(), a.getOriginalName()))
                .collect(Collectors.toSet());  // Dùng collect(Collectors.toSet()) để tạo Set
    }
}