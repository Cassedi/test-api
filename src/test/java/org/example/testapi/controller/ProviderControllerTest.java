package org.example.testapi.controller;

import org.example.testapi.model.Provider;
import org.example.testapi.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

@WebMvcTest(ProviderController.class)
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProviderService providerService;

    private Provider createTestProvider(Long id, String name, String serviceType) {
        Provider provider = new Provider();
        provider.setId(id);
        provider.setName(name);
        provider.setEmail(name.toLowerCase() + "@test.com");
        provider.setPhone("+1111111111");
        provider.setServiceType(serviceType);
        provider.setCreatedAt(LocalDateTime.now());
        return provider;
    }

    @Test
    void shouldReturnAllProviders() throws Exception {
        List<Provider> providers = List.of(
                createTestProvider(1L, "CloudCorp", "CLOUD"),
                createTestProvider(2L, "SecurIT", "SECURITY")
        );
        when(providerService.findAll()).thenReturn(providers);

        mockMvc.perform(get("/api/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("CloudCorp")))
                .andExpect(jsonPath("$[1].serviceType", is("SECURITY")));
    }

    @Test
    void shouldReturnProviderById() throws Exception {
        Provider provider = createTestProvider(1L, "CloudCorp", "CLOUD");
        when(providerService.findById(1L)).thenReturn(provider);

        mockMvc.perform(get("/api/providers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("CloudCorp")))
                .andExpect(jsonPath("$.serviceType", is("CLOUD")));
    }

    @Test
    void shouldReturn404WhenProviderNotFound() throws Exception {
        when(providerService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        mockMvc.perform(get("/api/providers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Provider not found")))
                .andExpect(jsonPath("$.path", is("/api/providers/99")));
    }

    @Test
    void shouldCreateProvider() throws Exception {
        Provider provider = createTestProvider(1L, "NewProvider", "ANALYTICS");
        when(providerService.create(any(Provider.class))).thenReturn(provider);

        mockMvc.perform(post("/api/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "NewProvider", "email": "new@test.com", "phone": "+3334445566", "serviceType": "ANALYTICS"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("NewProvider")))
                .andExpect(jsonPath("$.serviceType", is("ANALYTICS")));
    }

    @Test
    void shouldReturn422WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "serviceType": "CLOUD", "phone": "+1111111111"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'name' is required")));
    }

    @Test
    void shouldReturn422WhenServiceTypeIsBlank() throws Exception {
        mockMvc.perform(post("/api/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Provider", "serviceType": "", "phone": "+1111111111"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)))
                .andExpect(jsonPath("$.message", is("Field 'serviceType' is required")));
    }

    @Test
    void shouldUpdateProvider() throws Exception {
        Provider updated = createTestProvider(1L, "UpdatedProvider", "CLOUD");
        when(providerService.update(eq(1L), any(Provider.class))).thenReturn(updated);

        mockMvc.perform(put("/api/providers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "UpdatedProvider", "email": "upd@test.com", "phone": "+4445556677", "serviceType": "CLOUD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UpdatedProvider")));
    }

    @Test
    void shouldDeleteProvider() throws Exception {
        doNothing().when(providerService).delete(1L);

        mockMvc.perform(delete("/api/providers/1"))
                .andExpect(status().isNoContent());

        verify(providerService).delete(1L);
    }
}
