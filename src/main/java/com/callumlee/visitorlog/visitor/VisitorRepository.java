package com.callumlee.visitorlog.visitor;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    Optional<Visitor> findFirstByFullNameIgnoreCaseAndPhone(String fullName, String phone);

    Optional<Visitor> findFirstByFullNameIgnoreCaseAndPhoneIsNull(String fullName);
}
