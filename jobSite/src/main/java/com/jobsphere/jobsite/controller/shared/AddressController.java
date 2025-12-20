package com.jobsphere.jobsite.controller.shared;

import com.jobsphere.jobsite.dto.shared.AddressDto;
import com.jobsphere.jobsite.service.shared.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService service;

    @PostMapping
    public AddressDto create(@RequestBody AddressDto dto) {
        return service.create(dto);
    }
}