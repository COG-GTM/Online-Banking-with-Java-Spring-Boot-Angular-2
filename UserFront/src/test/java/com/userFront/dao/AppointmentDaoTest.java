package com.userFront.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userFront.domain.Appointment;
import com.userFront.domain.AppointmentSummary;
import com.userFront.domain.Recipient;
import com.userFront.domain.User;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.generate_statistics=true"
})
public class AppointmentDaoTest {

    private static final int USERS = 30;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentDao appointmentDao;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @Before
    public void setUp() {
        for (int i = 0; i < USERS; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("secret-hash");
            entityManager.persist(user);

            Recipient recipient = new Recipient();
            recipient.setName("recipient" + i);
            recipient.setUser(user);
            entityManager.persist(recipient);

            Appointment appointment = new Appointment();
            appointment.setDate(new Date(1000L * i));
            appointment.setDescription("appointment" + i);
            appointment.setLocation("branch");
            appointment.setUser(user);
            entityManager.persist(appointment);
        }
        entityManager.flush();
        entityManager.clear();

        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    public void loadsOneBoundedPageInTwoStatements() throws Exception {
        Page<AppointmentSummary> page = appointmentDao.findAllSummaries(
                new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "date", "id")));
        String json = new ObjectMapper().writeValueAsString(page.getContent());

        List<AppointmentSummary> content = page.getContent();
        assertEquals(10, content.size());
        assertEquals(USERS, page.getTotalElements());
        assertEquals("appointment29", content.get(0).getDescription());
        assertEquals("user29", content.get(0).getUser().getUsername());
        assertEquals(2, statistics.getPrepareStatementCount());
        assertTrue(json.contains("\"username\":\"user29\""));
        assertFalse(json.contains("secret-hash"));
        assertFalse(json.contains("recipient"));
    }
}
