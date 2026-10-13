package com.callumlee.visitorlog.resident;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {

    List<Resident> findByActiveTrueOrderByFullNameAsc();
}
