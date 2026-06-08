package com.amalitech.todoApi.controller;

import com.amalitech.todoApi.dto.request.TodoRequest;
import com.amalitech.todoApi.models.Todo;
import com.amalitech.todoApi.models.Tag;
import com.amalitech.todoApi.service.TodoService;
import com.amalitech.todoApi.service.interfaces.TodoServiceInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
@RequestMapping("/")
public class TodoController {
    private final TodoServiceInterface todoService;

    @GetMapping
    public String getTodos(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model) {

        Page<Todo> todoPage = todoService.getTodos(keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("todos", todoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", todoPage.getTotalPages());
        model.addAttribute("totalItems", todoPage.getTotalElements());
        model.addAttribute("keyword", keyword);

        return "todos/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("todoRequest", new TodoRequest());
        model.addAttribute("categories", todoService.getAllCategories());
        return "todos/create";
    }

    @PostMapping
    public String createTodo(@Valid @ModelAttribute TodoRequest request) {
        todoService.createTodo(request);
        return "redirect:/";
    }

    @GetMapping("/view/{id}")
    public String viewTodo(@PathVariable("id") Long id, Model model) {
        model.addAttribute("todo", todoService.getTodoById(id));
        return "todos/view";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Todo todo = todoService.getTodoById(id);
        TodoRequest request = new TodoRequest();
        request.setTitle(todo.getTitle());
        request.setContent(todo.getContent());
        if (todo.getCategory() != null) {
            request.setCategoryId(todo.getCategory().getId());
        }
        if (todo.getTags() != null) {
            request.setTags(todo.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")));
        }

        model.addAttribute("todoRequest", request);
        model.addAttribute("todoId", id);
        model.addAttribute("categories", todoService.getAllCategories());
        return "todos/edit";
    }

    @PostMapping("/update/{id}")
    public String updateTodo(@PathVariable("id") Long id, @Valid @ModelAttribute TodoRequest request) {
        todoService.updateTodo(id, request);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteTodo(@PathVariable("id") Long id) {
        todoService.deleteTodo(id);
        return "redirect:/";
    }
}