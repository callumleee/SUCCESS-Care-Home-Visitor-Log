package com.callumlee.visitorlog.visit;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByTimeOutIsNullOrderByTimeInAsc();

    List<Visit> findByTimeInBetweenOrderByTimeInAsc(LocalDateTime start, LocalDateTime end);

    List<Visit> findAllByOrderByTimeInDesc();

    List<Visit> findByResidentIdOrderByTimeInDesc(Long residentId);

    List<Visit> findByTimeOutIsNotNull();

    long countByTimeOutIsNull();

    boolean existsByVisitorIdAndResidentIdAndTimeOutIsNull(Long visitorId, Long residentId);

    @Query("select v.resident.fullName as name, count(v) as count from Visit v "
            + "group by v.resident.id, v.resident.fullName order by count(v) desc")
    List<NameCount> countVisitsPerResident();

    @Query("select v.visitor.fullName as name, count(v) as count from Visit v "
            + "group by v.visitor.id, v.visitor.fullName order by count(v) desc")
    List<NameCount> countVisitsPerVisitor();

    // A name and how many visits it has (used for stats)
    interface NameCount {
        String getName();

        long getCount();
    }
}
