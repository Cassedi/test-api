package org.example.testapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaskRequest(
        @NotBlank(message = "Field 'title' is required")
        String title,

        String description,

        String status,

        @NotNull(message = "Field 'clientId' is required")
        Long clientId,

        Long providerId
) {
}
