package org.example.testapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Field 'name' is required")
    private String name;

    @Email(message = "Field 'email' must be a valid email address")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\-]{7,15}$", message = "Field 'phone' must be a valid phone number")
    private String phone;

    @NotBlank(message = "Field 'serviceType' is required")
    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "provider")
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
