package com.callumlee.visitorlog.visit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Body of POST /api/visits
public record SignInRequest(
        @NotBlank @Size(max = 100) String visitorName,
        @Size(max = 30) String visitorPhone,
        @NotNull Long residentId) {
}
