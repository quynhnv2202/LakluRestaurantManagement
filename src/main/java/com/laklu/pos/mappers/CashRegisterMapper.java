package com.laklu.pos.mappers;

import com.laklu.pos.dataObjects.response.CashRegisterResponse;
import com.laklu.pos.entities.CashRegister;
import com.laklu.pos.entities.Profile;
import com.laklu.pos.repositories.ProfileRepository;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CashRegisterMapper {
    private final ProfileRepository profileRepository;

    public CashRegisterResponse toResponse(CashRegister entity) {
        if (entity == null) {
            return null;
        }
        
        String userFullName = null;
        if (entity.getUser() != null) {
            Optional<Profile> profile = profileRepository.findByUserId(entity.getUser().getId());
            if (profile.isPresent()) {
                userFullName = profile.get().getFullName();
            }
        }
        
        return new CashRegisterResponse(
                entity.getId(),
                entity.getUser() != null ? entity.getUser().getId() : null,
                userFullName,
                entity.getSchedule() != null ? entity.getSchedule().getId() : null,
                entity.getInitialAmount(),
                entity.getCurrentAmount(),
                entity.getShiftStart(),
                entity.getShiftEnd(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<CashRegisterResponse> toResponseList(List<CashRegister> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
} 