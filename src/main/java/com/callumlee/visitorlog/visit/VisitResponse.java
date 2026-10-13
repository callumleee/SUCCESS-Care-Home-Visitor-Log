package com.callumlee.visitorlog.visit;

import java.time.LocalDateTime;

// JSON returned for a visit
public record VisitResponse(
        Long id,
        String visitorName,
        Long residentId,
        String residentName,
        String room,
        LocalDateTime timeIn,
        LocalDateTime timeOut,
        boolean signedIn,
        long durationMinutes) {

    public static VisitResponse from(Visit visit) {
        return new VisitResponse(
                visit.getId(),
                visit.getVisitor().getFullName(),
                visit.getResident().getId(),
                visit.getResident().getFullName(),
                visit.getResident().getRoom(),
                visit.getTimeIn(),
                visit.getTimeOut(),
                visit.isSignedIn(),
                visit.getDurationMinutes());
    }
}
