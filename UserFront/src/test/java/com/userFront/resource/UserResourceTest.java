package com.userFront.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.userFront.domain.Recipient;
import com.userFront.domain.User;
import com.userFront.service.TransactionService;
import com.userFront.service.UserService;

public class UserResourceTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private UserResource userResource;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    @Test
    public void userListReturnsFirstPageWithDefaultSizeAndTotals() throws Exception {
        List<User> users = new ArrayList<>();
        users.add(user("alice"));
        users.add(user("bob"));
        when(userService.findUserList(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(users, (Pageable) inv.getArguments()[0], 120));

        mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(header().string("X-Total-Count", "120"))
                .andExpect(header().string("X-Page", "0"))
                .andExpect(header().string("X-Page-Size", String.valueOf(UserResource.DEFAULT_PAGE_SIZE)))
                .andExpect(header().string("X-Total-Pages", "3"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).findUserList(pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(UserResource.DEFAULT_PAGE_SIZE, pageable.getValue().getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getValue().getSort().getOrderFor("userId").getDirection());
    }

    @Test
    public void userListPassesRequestedPage() throws Exception {
        when(userService.findUserList(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(Collections.<User>emptyList(), (Pageable) inv.getArguments()[0], 0));

        mockMvc.perform(get("/api/user/all").param("page", "2").param("size", "25"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).findUserList(pageable.capture());
        assertEquals(2, pageable.getValue().getPageNumber());
        assertEquals(25, pageable.getValue().getPageSize());
    }

    @Test
    public void userListRejectsOutOfRangePaging() throws Exception {
        mockMvc.perform(get("/api/user/all").param("size", String.valueOf(UserResource.MAX_PAGE_SIZE + 1)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/user/all").param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/user/all").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findUserList(any(Pageable.class));
    }

    @Test
    public void userListDoesNotSerializeRecipients() throws Exception {
        User alice = user("alice");
        alice.setRecipientList(Collections.singletonList(new Recipient()));
        when(userService.findUserList(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(Collections.singletonList(alice), (Pageable) inv.getArguments()[0], 1));

        mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recipientList").doesNotExist())
                .andExpect(jsonPath("$[0].userRoles").doesNotExist());
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        return user;
    }
}
