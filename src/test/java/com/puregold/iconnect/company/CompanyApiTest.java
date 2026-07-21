// src/test/java/com/puregold/iconnect/company/CompanyApiTest.java
package com.puregold.iconnect.company;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createThenList() throws Exception {
        String body = """
            {"code":"TESTCO","legalName":"Test Co Inc.","baseCurrency":"PHP","status":"Active"}
            """;
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.code").value("TESTCO"))
           .andExpect(jsonPath("$.id").isNumber());

        mvc.perform(get("/api/companies"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='TESTCO')]").exists());
    }

    @Test
    void rejectsBlankLegalName() throws Exception {
        String body = """
            {"code":"BAD","legalName":"","baseCurrency":"PHP"}
            """;
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.legalName").exists());
    }

    @Test
    void rejectsDuplicateCode() throws Exception {
        String body = """
            {"code":"DUP","legalName":"First","baseCurrency":"PHP"}
            """;
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated());
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
