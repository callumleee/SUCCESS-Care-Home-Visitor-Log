package com.callumlee.visitorlog.resident;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.callumlee.visitorlog.common.NotFoundException;
import com.callumlee.visitorlog.visit.VisitResponse;
import com.callumlee.visitorlog.visit.VisitService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    private final ResidentRepository residents;
    private final VisitService visitService;

    public ResidentController(ResidentRepository residents, VisitService visitService) {
        this.residents = residents;
        this.visitService = visitService;
    }

    // Active residents, for the sign-in screen's resident search
    @GetMapping
    public List<ResidentResponse> list() {
        return residents.findByActiveTrueOrderByFullNameAsc().stream().map(ResidentResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResidentResponse create(@Valid @RequestBody ResidentRequest request) {
        Resident resident = new Resident(request.fullName().trim(), request.room());
        return ResidentResponse.from(residents.save(resident));
    }

    // Deactivates (does not delete) a resident so their visit history is kept
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void deactivate(@PathVariable Long id) {
        Resident resident = residents.findById(id)
                .orElseThrow(() -> new NotFoundException("Resident not found: " + id));
        resident.deactivate();
    }

    @GetMapping("/{id}/visits")
    public List<VisitResponse> visits(@PathVariable Long id) {
        return visitService.findVisitsForResident(id);
    }
}
