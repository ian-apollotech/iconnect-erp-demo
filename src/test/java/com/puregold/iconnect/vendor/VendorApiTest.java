// src/test/java/com/puregold/iconnect/vendor/VendorApiTest.java
package com.puregold.iconnect.vendor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VendorApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createThenList() throws Exception {
        String body = """
            {"name":"Test Vendor Co","tin":"123-456-789","deliveryModel":"Direct-to-Store (DTS)",
             "paymentTerms":"30 Days","leadTime":"3 Days","minOrder":100.00,"rtvAllowed":"Yes",
             "registeredAddress":"1 Vendor St","contactPerson":"Jane Doe","phone":"09171234567",
             "email":"jane@vendor.com","status":"Active"}
            """;

        mvc.perform(post("/api/vendors").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.name").value("Test Vendor Co"))
           .andExpect(jsonPath("$.id").isNumber())
           .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.startsWith("V-")));

        mvc.perform(get("/api/vendors"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.name=='Test Vendor Co')]").exists());
    }

    @Test
    void rejectsBlankName() throws Exception {
        String body = """
            {"name":"","rtvAllowed":"Yes"}
            """;
        mvc.perform(post("/api/vendors").contentType("application/json").content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.name").exists());
    }
}
