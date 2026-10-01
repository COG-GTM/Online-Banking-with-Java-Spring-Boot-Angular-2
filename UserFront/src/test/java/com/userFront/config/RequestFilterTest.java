package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

public class RequestFilterTest {

    private final RequestFilter filter = new RequestFilter();
    private final ByteArrayOutputStream console = new ByteArrayOutputStream();
    private final ListAppender<ILoggingEvent> appender = new ListAppender<ILoggingEvent>();
    private Logger logger;
    private Level originalLevel;
    private boolean originalAdditive;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @Before
    public void setUp() {
        logger = (Logger) LoggerFactory.getLogger(RequestFilter.class);
        originalLevel = logger.getLevel();
        originalAdditive = logger.isAdditive();
        logger.setLevel(Level.DEBUG);
        logger.setAdditive(false);
        appender.start();
        logger.addAppender(appender);
        originalOut = System.out;
        originalErr = System.err;
        PrintStream capture = new PrintStream(console, true);
        System.setOut(capture);
        System.setErr(capture);
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
        logger.setAdditive(originalAdditive);
    }

    @Test
    public void preflightIsAnsweredAndLoggedAtDebugWithoutConsoleOutput() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/user/all");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest req, ServletResponse res) {
                throw new AssertionError("pre-flight must not reach the chain");
            }
        });

        assertEquals(200, response.getStatus());
        assertEquals("POST,GET,DELETE", response.getHeader("Access-Control-Allow-Methods"));
        assertEquals("3600", response.getHeader("Access-Control-Max-Age"));
        assertEquals(1, appender.list.size());
        assertEquals(Level.DEBUG, appender.list.get(0).getLevel());
        assertEquals("Pre-flight /api/user/all", appender.list.get(0).getFormattedMessage());
        assertEquals("", console.toString());
    }

    @Test
    public void chainFailureIsLoggedAtErrorWithCauseWithoutConsoleOutput() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/all");
        MockHttpServletResponse response = new MockHttpServletResponse();
        final IOException failure = new IOException("boom");

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest req, ServletResponse res) throws IOException {
                throw failure;
            }
        });

        assertEquals(1, appender.list.size());
        ILoggingEvent event = appender.list.get(0);
        assertEquals(Level.ERROR, event.getLevel());
        assertEquals("Request GET /api/user/all failed", event.getFormattedMessage());
        assertSame(failure, ((ThrowableProxy) event.getThrowableProxy()).getThrowable());
        assertEquals("", console.toString());
    }

    @Test
    public void successfulRequestPassesThroughWithoutLogging() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/all");
        MockHttpServletResponse response = new MockHttpServletResponse();
        final boolean[] invoked = {false};

        filter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest req, ServletResponse res) {
                invoked[0] = true;
            }
        });

        assertEquals(true, invoked[0]);
        assertEquals("http://localhost:4200", response.getHeader("Access-Control-Allow-Origin"));
        assertFalse(appender.list.iterator().hasNext());
        assertEquals("", console.toString());
    }
}
