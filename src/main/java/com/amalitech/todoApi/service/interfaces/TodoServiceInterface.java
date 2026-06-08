package com.amalitech.todoApi.service.interfaces;

import com.amalitech.todoApi.dto.request.TodoRequest;
import com.amalitech.todoApi.models.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import com.amalitech.todoApi.models.Category;


public interface  TodoServiceInterface {
    Todo createTodo(TodoRequest todo);
    List<Todo> getAllTodos();
    Page<Todo> getTodos(String keyword, Pageable pageable);
    Todo getTodoById(Long id);
    Todo updateTodo(Long id, TodoRequest todo);
    void deleteTodo(Long id);
    // In TodoServiceInterface.java

    List<Category> getAllCategories();
}
