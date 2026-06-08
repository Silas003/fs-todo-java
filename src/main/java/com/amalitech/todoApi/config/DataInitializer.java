package com.amalitech.todoApi.config;

import com.amalitech.todoApi.models.Category;
import com.amalitech.todoApi.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AllArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;

    @PostConstruct
    public void init() {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                new Category("Work"),
                new Category("Personal"),
                new Category("Idea"),
                new Category("Todo")
            ));
        }
    }
}
