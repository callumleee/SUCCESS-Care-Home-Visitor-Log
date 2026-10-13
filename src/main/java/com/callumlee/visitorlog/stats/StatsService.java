package com.callumlee.visitorlog.stats;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.callumlee.visitorlog.visit.Visit;
import com.callumlee.visitorlog.visit.VisitRepository;
import com.callumlee.visitorlog.visit.VisitRepository.NameCount;

@Service
public class StatsService {

    private final VisitRepository visits;

    public StatsService(VisitRepository visits) {
        this.visits = visits;
    }

    @Transactional(readOnly = true)
    public VisitStats getStats() {
        return new VisitStats(
                visits.count(),
                visits.countByTimeOutIsNull(),
                averageVisitMinutes(),
                topName(visits.countVisitsPerResident()),
                topName(visits.countVisitsPerVisitor()));
    }

    // Average length of completed visits, or 0 if no visits have been completed
    private double averageVisitMinutes() {
        List<Visit> completed = visits.findByTimeOutIsNotNull();
        return completed.stream().mapToLong(Visit::getDurationMinutes).average().orElse(0);
    }

    private static String topName(List<NameCount> counts) {
        return counts.isEmpty() ? null : counts.get(0).getName();
    }
}
