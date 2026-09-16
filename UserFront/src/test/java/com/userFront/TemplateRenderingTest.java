package com.userFront;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ThymeleafAutoConfiguration.class)
public class TemplateRenderingTest {

    @Autowired
    private TemplateEngine engine;

    private Context context;

    @Before
    public void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);

        engine.setTemplateResolver(resolver);
        engine.setLinkBuilder(new TestLinkBuilder());

        context = new Context();
        context.setVariables(model());
    }

    @Test
    public void rendersAllPageTemplates() {
        for (String template : Arrays.asList(
                "appointment",
                "betweenAccounts",
                "deposit",
                "index",
                "primaryAccount",
                "profile",
                "recipient",
                "savingsAccount",
                "signup",
                "toSomeoneElse",
                "userFront",
                "withdraw")) {
            String output = engine.process(template, context);

            assertFalse(template + " rendered empty output", output.trim().isEmpty());
            assertFalse(template + " contains an unresolved expression", output.contains("${"));
            assertFalse(template + " contains an unresolved Thymeleaf attribute", output.contains("th:"));

            if (!Arrays.asList("index", "signup").contains(template)) {
                assertTrue(template + " is missing the logout form",
                        output.contains("<form") && output.contains("action=\"/logout\""));
            }
        }
    }

    private Map<String, Object> model() {
        Map<String, Object> primaryAccount = account("1001");
        Map<String, Object> savingsAccount = account("2001");

        Map<String, Object> user = new HashMap<String, Object>();
        user.put("firstName", "Ada");
        user.put("lastName", "Lovelace");
        user.put("phone", "555-0100");
        user.put("email", "ada@example.com");
        user.put("username", "ada");
        user.put("password", "password");
        user.put("userId", 1L);
        user.put("primaryAccount", primaryAccount);
        user.put("savingsAccount", savingsAccount);

        Map<String, Object> recipient = new HashMap<String, Object>();
        recipient.put("id", 1L);
        recipient.put("name", "Grace Hopper");
        recipient.put("email", "grace@example.com");
        recipient.put("phone", "555-0101");
        recipient.put("accountNumber", "3001");
        recipient.put("description", "Payroll");

        Map<String, Object> appointment = new HashMap<String, Object>();
        appointment.put("id", 1L);
        appointment.put("location", "Boston");
        appointment.put("description", "Discuss account options");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("primaryAccount", primaryAccount);
        model.put("savingsAccount", savingsAccount);
        model.put("user", user);
        model.put("recipient", recipient);
        model.put("recipientList", Arrays.<Map<String, Object>>asList(recipient));
        model.put("appointment", appointment);
        model.put("primaryTransactionList", Arrays.<Map<String, Object>>asList(transaction()));
        model.put("savingsTransactionList", Arrays.<Map<String, Object>>asList(transaction()));
        model.put("dateString", "2024-01-01 10:00");
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("error", null);
        param.put("logout", null);
        model.put("param", param);
        model.put("emailExists", false);
        model.put("usernameExists", false);
        model.put("amount", "");
        return model;
    }

    private Map<String, Object> account(String accountNumber) {
        Map<String, Object> account = new HashMap<String, Object>();
        account.put("accountNumber", accountNumber);
        account.put("accountBalance", "100.00");
        return account;
    }

    private Map<String, Object> transaction() {
        Map<String, Object> transaction = new HashMap<String, Object>();
        transaction.put("date", "2024-01-01");
        transaction.put("description", "Deposit");
        transaction.put("type", "CREDIT");
        transaction.put("status", "COMPLETED");
        transaction.put("amount", "100.00");
        transaction.put("availableBalance", "100.00");
        return transaction;
    }

    private static class TestLinkBuilder implements ILinkBuilder {

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public Integer getOrder() {
            return 1;
        }

        @Override
        public String buildLink(IExpressionContext context, String base, Map<String, Object> parameters) {
            if (parameters == null || parameters.isEmpty()) {
                return base;
            }

            StringBuilder link = new StringBuilder(base);
            link.append(base.indexOf('?') >= 0 ? '&' : '?');
            boolean first = true;
            for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
                if (!first) {
                    link.append('&');
                }
                link.append(parameter.getKey()).append('=').append(encode(parameter.getValue()));
                first = false;
            }
            return link.toString();
        }

        private String encode(Object value) {
            try {
                return URLEncoder.encode(String.valueOf(value), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
