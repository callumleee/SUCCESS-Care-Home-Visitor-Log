package com.callumlee.visitorlog.resident;

public record ResidentResponse(Long id, String fullName, String room, boolean active) {

    public static ResidentResponse from(Resident resident) {
        return new ResidentResponse(resident.getId(), resident.getFullName(), resident.getRoom(),
                resident.isActive());
    }
}
