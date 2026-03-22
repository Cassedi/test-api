package org.example.testapi.controller;

import org.example.testapi.dto.TaskRequest;
import org.example.testapi.model.Client;
import org.example.testapi.model.Provider;
import org.example.testapi.model.Task;
import org.example.testapi.security.JwtAuthenticationFilter;
import org.example.testapi.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Task createTestTask(Long id, String title, String status) {
        Client client = new Client();
        client.setId(1L);
        client.setName("John");
        client.setEmail("john@test.com");
        client.setCreatedAt(LocalDateTime.now());

        Provider provider = new Provider();
        provider.setId(1L);
        provider.setName("CloudCorp");
        provider.setServiceType("CLOUD");
        provider.setCreatedAt(LocalDateTime.now());

        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Test description");
        task.setStatus(status);
        task.setClient(client);
        task.setProvider(provider);
        return task;
    }

    @Test
    void shouldReturnAllTasks() throws Exception {
        List<Task> tasks = List.of(
                createTestTask(1L, "Task 1", "NEW"),
                createTestTask(2L, "Task 2", "IN_PROGRESS")
        );
        when(taskService.findAll()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].status", is("IN_PROGRESS")));
    }

    @Test
    void shouldReturnTaskById() throws Exception {
        Task task = createTestTask(1L, "Task 1", "NEW");
        when(taskService.findById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Task 1")))
                .andExpect(jsonPath("$.client.name", is("John")))
                .andExpect(jsonPath("$.provider.name", is("CloudCorp")));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        when(taskService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Task not found")))
                .andExpect(jsonPath("$.path", is("/api/tasks/99")));
    }

    @Test
    void shouldCreateTask() throws Exception {
        Task task = createTestTask(1L, "New Task", "NEW");
        when(taskService.create(any(TaskRequest.class))).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "New Task", "description": "desc", "status": "NEW", "clientId": 1, "providerId": 1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.status", is("NEW")));
    }

    @Test
    void shouldReturn422WhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "clientId": 1}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'title' is required")));
    }

    @Test
    void shouldReturn422WhenClientIdIsNull() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Some Task"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'clientId' is required")));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        Task updated = createTestTask(1L, "Updated Task", "DONE");
        when(taskService.update(eq(1L), any(TaskRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Updated Task", "description": "upd", "status": "DONE", "clientId": 1, "providerId": 1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Task")))
                .andExpect(jsonPath("$.status", is("DONE")));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L);
    }
}
