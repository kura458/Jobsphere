package com.jobsphere.jobsite.service.shared;

import com.jobsphere.jobsite.dto.shared.AddressDto;
import com.jobsphere.jobsite.model.shared.Address;
import com.jobsphere.jobsite.repository.shared.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;

    @Transactional
    public AddressDto create(AddressDto dto) {
        Address address = Address.builder()
            .country(dto.getCountry())
            .region(dto.getRegion())
            .city(dto.getCity())
            .subCity(dto.getSubCity())
            .street(dto.getStreet())
            .createdAt(Instant.now())
            .build();
        Address saved = addressRepository.save(address);
        return AddressDto.builder()
            .id(saved.getId())
            .country(saved.getCountry())
            .region(saved.getRegion())
            .city(saved.getCity())
            .subCity(saved.getSubCity())
            .street(saved.getStreet())
            .build();
    }
}