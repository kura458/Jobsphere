package com.jobsphere.jobsite.dto.seeker;

import com.jobsphere.jobsite.constant.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BasicInfoRequest {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String middleName;

    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^[+]?[0-9]{10,15}$")
    private String phone;

    @NotNull
    private Gender gender;

    @NotNull
    private LocalDate dateOfBirth;
}