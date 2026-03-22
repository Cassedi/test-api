package org.example.testapi.controller;

import org.example.testapi.model.Client;
import org.example.testapi.security.JwtAuthenticationFilter;
import org.example.testapi.service.ClientService;
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

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private Client createTestClient(Long id, String name, String email) {
        Client client = new Client();
        client.setId(id);
        client.setName(name);
        client.setEmail(email);
        client.setPhone("+1234567890");
        client.setCompany("Test Corp");
        client.setCreatedAt(LocalDateTime.now());
        return client;
    }

    @Test
    void shouldReturnAllClients() throws Exception {
        List<Client> clients = List.of(
                createTestClient(1L, "John", "john@test.com"),
                createTestClient(2L, "Jane", "jane@test.com")
        );
        when(clientService.findAll()).thenReturn(clients);

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John")))
                .andExpect(jsonPath("$[1].name", is("Jane")));
    }

    @Test
    void shouldReturnClientById() throws Exception {
        Client client = createTestClient(1L, "John", "john@test.com");
        when(clientService.findById(1L)).thenReturn(client);

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john@test.com")));
    }

    @Test
    void shouldReturn404WhenClientNotFound() throws Exception {
        when(clientService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Client not found")))
                .andExpect(jsonPath("$.path", is("/api/clients/99")));
    }

    @Test
    void shouldCreateClient() throws Exception {
        Client client = createTestClient(1L, "John", "john@test.com");
        when(clientService.create(any(Client.class))).thenReturn(client);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John", "email": "john@test.com", "phone": "+1234567890", "company": "Test Corp"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")));
    }

    @Test
    void shouldReturn422WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "email": "john@test.com", "phone": "+1234567890"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'name' is required")))
                .andExpect(jsonPath("$.path", is("/api/clients")));
    }

    @Test
    void shouldReturn422WhenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John", "email": "not-an-email", "phone": "+1234567890"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'email' must be a valid email address")));
    }

    @Test
    void shouldReturn422WhenPhoneInvalid() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "John", "email": "john@test.com", "phone": "abc"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'phone' must be a valid phone number")));
    }

    @Test
    void shouldUpdateClient() throws Exception {
        Client updated = createTestClient(1L, "Updated", "updated@test.com");
        when(clientService.update(eq(1L), any(Client.class))).thenReturn(updated);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated", "email": "updated@test.com", "phone": "+1112223344", "company": "New Corp"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated")));
    }

    @Test
    void shouldDeleteClient() throws Exception {
        doNothing().when(clientService).delete(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService).delete(1L);
    }
}
