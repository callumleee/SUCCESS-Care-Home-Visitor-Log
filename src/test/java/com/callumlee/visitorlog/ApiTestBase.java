package com.callumlee.visitorlog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Shared setup for API tests: full app with an in-memory database (no demo data).
// Each test runs in a transaction that is rolled back afterwards.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration")
public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mvc;
}
