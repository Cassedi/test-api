package org.example.testapi.service;

import lombok.RequiredArgsConstructor;
import org.example.testapi.model.Provider;
import org.example.testapi.repository.ProviderRepository;
import org.example.testapi.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final TaskRepository taskRepository;

    public List<Provider> findAll() {
        return providerRepository.findAll();
    }

    public Provider findById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));
    }

    public Provider create(Provider provider) {
        return providerRepository.save(provider);
    }

    public Provider update(Long id, Provider provider) {
        Provider existing = findById(id);
        existing.setName(provider.getName());
        existing.setEmail(provider.getEmail());
        existing.setPhone(provider.getPhone());
        existing.setServiceType(provider.getServiceType());
        return providerRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!providerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found");
        }
        taskRepository.clearProviderByProviderId(id);
        providerRepository.deleteById(id);
    }
}
