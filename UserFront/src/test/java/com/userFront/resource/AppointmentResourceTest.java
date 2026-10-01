package com.userFront.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.AppointmentSummary;
import com.userFront.service.AppointmentService;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentResourceTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentResource appointmentResource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentResource).build();
    }

    @Test
    public void returnsOnePageOfSummariesWithPagingHeaders() throws Exception {
        AppointmentSummary summary = new AppointmentSummary(7L, new Date(0), "Branch 1", "Loan", false, "alice");
        when(appointmentService.findAllSummaries(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(summary), new org.springframework.data.domain.PageRequest(1, 10), 11));

        mockMvc.perform(get("/api/appointment/all").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "11"))
                .andExpect(header().string("X-Page", "1"))
                .andExpect(header().string("X-Page-Size", "10"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].description").value("Loan"))
                .andExpect(jsonPath("$[0].confirmed").value(false))
                .andExpect(jsonPath("$[0].user.username").value("alice"))
                .andExpect(jsonPath("$[0].user.password").doesNotExist())
                .andExpect(jsonPath("$[0].user.recipientList").doesNotExist());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentService).findAllSummaries(pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("date").getDirection());
        assertEquals(Sort.Direction.DESC, pageable.getValue().getSort().getOrderFor("id").getDirection());
    }

    @Test
    public void defaultsToFirstPageOfDefaultSize() throws Exception {
        when(appointmentService.findAllSummaries(any(Pageable.class)))
                .thenReturn(new PageImpl<AppointmentSummary>(Arrays.<AppointmentSummary>asList()));

        mockMvc.perform(get("/api/appointment/all"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Page", "0"))
                .andExpect(header().string("X-Page-Size", String.valueOf(AppointmentResource.DEFAULT_PAGE_SIZE)));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentService).findAllSummaries(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(AppointmentResource.DEFAULT_PAGE_SIZE, pageable.getValue().getPageSize());
    }

    @Test
    public void rejectsOutOfRangePaging() throws Exception {
        mockMvc.perform(get("/api/appointment/all").param("size", String.valueOf(AppointmentResource.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/appointment/all").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/appointment/all").param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/appointment/all").param("page", "abc"))
                .andExpect(status().isBadRequest());

        verifyZeroInteractions(appointmentService);
    }
}
