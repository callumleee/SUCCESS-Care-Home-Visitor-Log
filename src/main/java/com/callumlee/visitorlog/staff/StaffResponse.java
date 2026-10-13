package com.callumlee.visitorlog.staff;

public record StaffResponse(Long id, String username, Role role) {

    public static StaffResponse from(StaffUser user) {
        return new StaffResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
