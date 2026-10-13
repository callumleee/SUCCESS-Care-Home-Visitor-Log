package com.callumlee.visitorlog.visit;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.callumlee.visitorlog.common.ConflictException;
import com.callumlee.visitorlog.common.NotFoundException;
import com.callumlee.visitorlog.resident.Resident;
import com.callumlee.visitorlog.resident.ResidentRepository;
import com.callumlee.visitorlog.visitor.Visitor;
import com.callumlee.visitorlog.visitor.VisitorRepository;

@Service
public class VisitService {
    private static final Logger log = LoggerFactory.getLogger(VisitService.class);

    private final VisitRepository visits;
    private final VisitorRepository visitors;
    private final ResidentRepository residents;
    private final Clock clock;

    public VisitService(VisitRepository visits, VisitorRepository visitors,
            ResidentRepository residents, Clock clock) {
        this.visits = visits;
        this.visitors = visitors;
        this.residents = residents;
        this.clock = clock;
    }

    // Signs a visitor in to see a resident. Reuses the visitor record if they have visited before.
    @Transactional
    public VisitResponse signIn(SignInRequest request) {
        Resident resident = residents.findById(request.residentId())
                .filter(Resident::isActive)
                .orElseThrow(() -> new NotFoundException("Resident not found: " + request.residentId()));

        String name = request.visitorName().trim();
        String phone = normalizePhone(request.visitorPhone());
        Visitor visitor = findOrCreateVisitor(name, phone);

        if (visits.existsByVisitorIdAndResidentIdAndTimeOutIsNull(visitor.getId(), resident.getId())) {
            throw new ConflictException(name + " is already signed in to visit " + resident.getFullName());
        }

        Visit visit = visits.save(new Visit(visitor, resident, LocalDateTime.now(clock)));
        log.info("Visitor signed in: visit {} (resident {})", visit.getId(), resident.getId());
        return VisitResponse.from(visit);
    }

    @Transactional
    public VisitResponse signOut(Long visitId) {
        Visit visit = visits.findById(visitId)
                .orElseThrow(() -> new NotFoundException("Visit not found: " + visitId));
        if (!visit.isSignedIn()) {
            throw new ConflictException("Visit " + visitId + " is already signed out");
        }
        visit.signOut(LocalDateTime.now(clock));
        log.info("Visitor signed out: visit {}", visitId);
        return VisitResponse.from(visit);
    }

    // Lists visits. active=true returns only visitors currently in the building;
    // date limits results to visits that started on that day.
    @Transactional(readOnly = true)
    public List<VisitResponse> findVisits(LocalDate date, boolean activeOnly) {
        List<Visit> result;
        if (activeOnly) {
            result = visits.findByTimeOutIsNullOrderByTimeInAsc();
            if (date != null) {
                result = result.stream().filter(v -> v.getTimeIn().toLocalDate().equals(date)).toList();
            }
        } else if (date != null) {
            result = visits.findByTimeInBetweenOrderByTimeInAsc(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        } else {
            result = visits.findAllByOrderByTimeInDesc();
        }
        return result.stream().map(VisitResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<VisitResponse> findVisitsForResident(Long residentId) {
        if (!residents.existsById(residentId)) {
            throw new NotFoundException("Resident not found: " + residentId);
        }
        return visits.findByResidentIdOrderByTimeInDesc(residentId).stream().map(VisitResponse::from).toList();
    }

    private Visitor findOrCreateVisitor(String name, String phone) {
        return (phone == null
                ? visitors.findFirstByFullNameIgnoreCaseAndPhoneIsNull(name)
                : visitors.findFirstByFullNameIgnoreCaseAndPhone(name, phone))
                .orElseGet(() -> visitors.save(new Visitor(name, phone)));
    }

    // Keeps only digits so "604-555-1234" and "(604) 555 1234" match; blank becomes null
    private static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }
}
