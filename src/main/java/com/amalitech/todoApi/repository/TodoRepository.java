package com.amalitech.todoApi.repository;

import com.amalitech.todoApi.models.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    @EntityGraph(attributePaths = {"category", "tags"})
    Optional<Todo> findById(Long id);

    @EntityGraph(attributePaths = {"category", "tags"})
    @Query(value = "SELECT n FROM Todo n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(n) FROM Todo n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Todo> searchTodos(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags"})
    Page<Todo> findAll(Pageable pageable);
}
