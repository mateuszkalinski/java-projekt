package com.example.librarysystem.controller;



import com.example.librarysystem.entity.Book;

import com.example.librarysystem.service.BookService;

import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.Optional;



@RestController

@RequestMapping("/api/projects")

@Tag(name = "Projects", description = "Endpoints for managing projects")

public class ProjectController {



    private final BookService bookService;



    public ProjectController(BookService bookService) {

        this.bookService = bookService;

    }



    @Operation(summary = "Get all projects", description = "Retrieve a list of all projects in the system")

    @GetMapping("/all")

    public List<Book> getAllProjects() {

        return bookService.getAllProjects();

    }



    @Operation(summary = "Get project by ID", description = "Retrieve a specific project by its ID")

    @GetMapping("/{id}")

    public ResponseEntity<Book> getProjectById(

            @Parameter(description = "ID of the project to be retrieved", required = true, example = "1")

            @PathVariable Long id) {

        Optional<Book> projectOptional = bookService.getProjectById(id);

        return projectOptional.map(ResponseEntity::ok)

                .orElseGet(() -> ResponseEntity.notFound().build());

    }



    @Operation(summary = "Create new project", description = "Create a new project with provided details")

    @PostMapping("/create")

    public ResponseEntity<Book> createProject(

            @Parameter(description = "Project object to create. Name is required.", required = true)

            @RequestBody Book project) {

        Book createdProject = bookService.createProject(project);

        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);

    }



    @Operation(summary = "Update project", description = "Update an existing project by its ID")

    @PutMapping("/update/{id}")

    public ResponseEntity<Book> updateProject(

            @Parameter(description = "ID of the project to update", required = true, example = "1")

            @PathVariable Long id,

            @Parameter(description = "Project object with updated information. Name is required.", required = true)

            @RequestBody Book projectDetails) {

        try {

            Book updatedProject = bookService.updateProject(id, projectDetails);

            if (updatedProject != null) {

                return ResponseEntity.ok(updatedProject);

            } else {

                return ResponseEntity.notFound().build();

            }

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }

    }



    @Operation(summary = "Delete project", description = "Delete a project by its ID")

    @DeleteMapping("/delete/{id}")

    public ResponseEntity<Void> deleteProject(

            @Parameter(description = "ID of the project to delete", required = true, example = "1")

            @PathVariable Long id) {

        try {

            bookService.deleteProject(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();

        }

    }



    @Operation(summary = "Add user to project", description = "Assigns an existing user to an existing project")

    @PostMapping("/{projectId}/users/{userId}") // lub .../users z ID usera w body

    public ResponseEntity<Book> addUserToProject(

            @Parameter(description = "ID of the project", required = true) @PathVariable Long projectId,

            @Parameter(description = "ID of the user to add", required = true) @PathVariable Long userId) {

        try {

            Book updatedProject = bookService.addUserToProject(projectId, userId);

            return ResponseEntity.ok(updatedProject);

        } catch (RuntimeException e) {

// Możesz tu dodać bardziej szczegółową obsługę błędów, np. zwracanie 404

            return ResponseEntity.notFound().build();

        }

    }

}