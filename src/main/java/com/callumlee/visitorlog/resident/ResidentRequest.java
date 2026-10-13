package com.callumlee.visitorlog.resident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Body of POST /api/residents
public record ResidentRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 20) String room) {
}
