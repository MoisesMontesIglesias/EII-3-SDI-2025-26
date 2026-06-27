package com.uniovi.sdi.reservationmanagement.services;

import com.uniovi.sdi.reservationmanagement.entities.Space;
import com.uniovi.sdi.reservationmanagement.entities.SpaceStatus;
import com.uniovi.sdi.reservationmanagement.repositories.SpaceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepository;

    public SpaceService(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    public Page<Space> findAvailableSpacesPage(String type, Integer minCapacity, int page, int size) {
        String normalizedType = type == null ? "" : type.trim();
        int normalizedCapacity = minCapacity == null || minCapacity < 1 ? 1 : minCapacity;
        return spaceRepository.findByStatusAndTypeContainingIgnoreCaseAndCapacityGreaterThanEqualOrderByNameAsc(
                SpaceStatus.ACTIVE,
                normalizedType,
                normalizedCapacity,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
    }

    public Page<Space> findAllSpacesPage(String type, Integer minCapacity, int page, int size) {
        String normalizedType = type == null ? "" : type.trim();
        int normalizedCapacity = minCapacity == null || minCapacity < 1 ? 1 : minCapacity;
        return spaceRepository.findByTypeContainingIgnoreCaseAndCapacityGreaterThanEqualOrderByNameAsc(
                normalizedType,
                normalizedCapacity,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
    }

    public void createIfMissing(String name, String type, String location, int capacity, SpaceStatus status, String description) {
        if (!spaceRepository.existsByNameIgnoreCase(name)) {
            spaceRepository.save(new Space(name, type, location, capacity, status, description));
        }
    }

    public void createSpace(String name, String type, String location, int capacity, SpaceStatus status, String description) {
        spaceRepository.save(new Space(name, type, location, capacity, status, description));
    }

    public Optional<Space> findActiveById(Long id) {
        return spaceRepository.findByIdAndStatus(id, SpaceStatus.ACTIVE);
    }

    public Optional<Space> findById(Long id) {
        return spaceRepository.findById(id);
    }

    public Optional<Space> findByName(String name) {
        return spaceRepository.findByNameIgnoreCase(name);
    }

    public List<Space> findAllSpacesOrderByName() {
        return spaceRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public void saveSpace(Space space) {
        spaceRepository.save(space);
    }

}
