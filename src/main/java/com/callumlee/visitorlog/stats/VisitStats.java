package com.callumlee.visitorlog.stats;

// Summary numbers for the admin dashboard. Names are null if there are no visits yet.
public record VisitStats(
        long totalVisits,
        long currentlySignedIn,
        double averageVisitMinutes,
        String mostVisitedResident,
        String mostFrequentVisitor) {
}
