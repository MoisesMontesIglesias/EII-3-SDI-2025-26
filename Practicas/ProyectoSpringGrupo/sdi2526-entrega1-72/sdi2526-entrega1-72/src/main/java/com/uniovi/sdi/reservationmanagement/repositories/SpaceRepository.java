package com.uniovi.sdi.reservationmanagement.repositories;

import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.entities.SpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SpaceRepository extends JpaRepository<Space, Long> {

    List<Space> findByStatusAndTypeContainingIgnoreCaseAndCapacityGreaterThanEqualOrderByNameAsc(
            SpaceStatus status,
            String type,
            Integer capacity
    );

    List<Space> findByTypeContainingIgnoreCaseAndCapacityGreaterThanEqualOrderByNameAsc(
            String type,
            Integer capacity
    );
    Page<Space> findByStatusAndTypeContainingIgnoreCaseAndCapacityGreaterThanEqualOrderByNameAsc(
            SpaceStatus status,
            String type,
            Integer capacity,
            Pageable pageable
    );

    Page<Space> findByTypeContainingIgnoreCaseAndCapacityGreaterThanEqualOrderByNameAsc(
            String type,
            Integer capacity,
            Pageable pageable
    );

    Page<Space> findAllByOrderByNameAsc(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndStatus(String name, SpaceStatus status);

    Optional<Space> findByIdAndStatus(Long id, SpaceStatus status);

    Optional<Space> findByNameIgnoreCase(String name);
}
