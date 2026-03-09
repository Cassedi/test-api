package org.example.testapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.testapi.model.Provider;
import org.example.testapi.service.ProviderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @GetMapping
    public List<Provider> findAll() {
        return providerService.findAll();
    }

    @GetMapping("/{id}")
    public Provider findById(@PathVariable Long id) {
        return providerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Provider create(@Valid @RequestBody Provider provider) {
        return providerService.create(provider);
    }

    @PutMapping("/{id}")
    public Provider update(@PathVariable Long id, @Valid @RequestBody Provider provider) {
        return providerService.update(id, provider);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        providerService.delete(id);
    }
}
