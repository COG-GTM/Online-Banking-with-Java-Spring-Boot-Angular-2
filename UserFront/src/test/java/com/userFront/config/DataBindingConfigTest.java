package com.userFront.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.util.AutoPopulatingList;
import org.springframework.web.bind.WebDataBinder;

public class DataBindingConfigTest {

    public static class Item {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Form {
        private String title;
        private List<Item> items = new AutoPopulatingList<Item>(Item.class);

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<Item> getItems() {
            return items;
        }

        public void setItems(List<Item> items) {
            this.items = items;
        }
    }

    private WebDataBinder hardenedBinder(Form form) {
        WebDataBinder binder = new WebDataBinder(form, "form");
        new DataBindingConfig().rejectIndexedPropertyPaths(binder);
        return binder;
    }

    @Test
    public void unhardenedBinderGrowsSelfPopulatingList() {
        Form form = new Form();
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("items[5000].name", "x");

        new WebDataBinder(form, "form").bind(values);

        assertEquals(5001, form.getItems().size());
    }

    @Test
    public void indexedPathIsSuppressedAndListDoesNotGrow() {
        Form form = new Form();
        WebDataBinder binder = hardenedBinder(form);
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("items[100000000].name", "x");

        binder.bind(values);

        assertEquals(0, form.getItems().size());
        assertTrue(Arrays.asList(binder.getBindingResult().getSuppressedFields()).contains("items[100000000].name"));
    }

    @Test
    public void mapStyleKeyPathIsSuppressed() {
        Form form = new Form();
        WebDataBinder binder = hardenedBinder(form);
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("items['1000000'].name", "x");

        binder.bind(values);

        assertEquals(0, form.getItems().size());
    }

    @Test
    public void plainFieldsStillBind() {
        Form form = new Form();
        WebDataBinder binder = hardenedBinder(form);
        MutablePropertyValues values = new MutablePropertyValues();
        values.add("title", "hello");

        binder.bind(values);

        assertEquals("hello", form.getTitle());
        assertEquals(0, binder.getBindingResult().getSuppressedFields().length);
    }
}
