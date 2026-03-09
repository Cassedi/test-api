package org.example.testapi.repository;

import org.example.testapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    @Query("UPDATE Task t SET t.provider = null WHERE t.provider.id = :providerId")
    void clearProviderByProviderId(@Param("providerId") Long providerId);
}
