package com.amalitech.todoApi.service;

import com.amalitech.todoApi.dto.request.TodoRequest;
import com.amalitech.todoApi.exceptions.InvalidTodoException;
import com.amalitech.todoApi.exceptions.TodoCreationException;
import com.amalitech.todoApi.exceptions.TodoNotFoundException;
import com.amalitech.todoApi.models.Category;
import com.amalitech.todoApi.models.Todo;
import com.amalitech.todoApi.models.Tag;
import com.amalitech.todoApi.repository.CategoryRepository;
import com.amalitech.todoApi.repository.TodoRepository;
import com.amalitech.todoApi.repository.TagRepository;
import com.amalitech.todoApi.service.interfaces.TodoServiceInterface;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TodoService implements TodoServiceInterface {
    private final TodoRepository todoRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    @Caching(
        put   = @CachePut(value = "todo", key = "#result.id"),
        evict = @CacheEvict(value = "todos", allEntries = true)
    )
    public Todo createTodo(TodoRequest request) {
        validateTodoRequest(request);

        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setContent(request.getContent());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new TodoNotFoundException("Category not found"));
            todo.setCategory(category);
        }

        if (request.getTags() != null && !request.getTags().isBlank()) {
            todo.setTags(processTags(request.getTags()));
        }

        try {
            return todoRepository.save(todo);
        } catch (Exception ex) {
            throw new TodoCreationException("Failed to create todo: " + ex.getMessage());
        }
    }

    private void validateTodoRequest(TodoRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new InvalidTodoException("Title cannot be empty");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new InvalidTodoException("Content cannot be empty");
        }
    }

    private Set<Tag> processTags(String tagsString) {
        return Arrays.stream(tagsString.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(new Tag(tagName))))
                .collect(Collectors.toSet());
    }

    @Override
    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    @Override
    public Page<Todo> getTodos(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return todoRepository.searchTodos(keyword, pageable);
        }
        return todoRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "todo", key = "#id")
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException("Todo not found"));
    }

    @Override
    @Transactional
    @Caching(
        put   = @CachePut(value = "todo", key = "#id"),
        evict = @CacheEvict(value = "todos", allEntries = true)
    )
    public Todo updateTodo(Long id, TodoRequest request) {
        validateTodoRequest(request);

        Todo existingTodo = getTodoById(id);
        existingTodo.setTitle(request.getTitle());
        existingTodo.setContent(request.getContent());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new TodoNotFoundException("Category not found"));
            existingTodo.setCategory(category);
        } else {
            existingTodo.setCategory(null);
        }

        if (request.getTags() != null) {
            existingTodo.setTags(processTags(request.getTags()));
        } else {
            existingTodo.getTags().clear();
        }

        return todoRepository.save(existingTodo);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "todo",  key = "#id"),
        @CacheEvict(value = "todos", allEntries = true)
    })
    public void deleteTodo(Long id) {
        Todo todo = getTodoById(id);
        todoRepository.delete(todo);
    }

    @Cacheable(value = "categories", key = "'all'")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
