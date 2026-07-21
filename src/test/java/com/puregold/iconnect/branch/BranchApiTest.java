// src/test/java/com/puregold/iconnect/branch/BranchApiTest.java
package com.puregold.iconnect.branch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BranchApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createCompany(String code) throws Exception {
        String body = """
            {"code":"%s","legalName":"Branch Test Co","baseCurrency":"PHP","status":"Active"}
            """.formatted(code);
        String response = mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void createThenList() throws Exception {
        Long companyId = createCompany("BRTESTCO");
        String body = """
            {"code":"BR001","name":"Test Branch","format":"Extra","region":"NCR","companyId":%d,
             "inventoryLocations":"Main WH","crossDockHub":"No","openingDate":"2020-01-01",
             "street":"123 Main St","city":"Quezon City","province":"Metro Manila","status":"Active"}
            """.formatted(companyId);

        mvc.perform(post("/api/branches").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.code").value("BR001"))
           .andExpect(jsonPath("$.id").isNumber())
           .andExpect(jsonPath("$.companyId").value(companyId));

        mvc.perform(get("/api/branches"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='BR001')]").exists());
    }

    @Test
    void rejectsNonExistentCompany() throws Exception {
        String body = """
            {"code":"BR002","name":"Test Branch 2","format":"Extra","region":"NCR","companyId":999999}
            """;
        mvc.perform(post("/api/branches").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankCode() throws Exception {
        Long companyId = createCompany("BRTESTCO2");
        String body = """
            {"code":"","name":"Test Branch 3","format":"Extra","region":"NCR","companyId":%d}
            """.formatted(companyId);
        mvc.perform(post("/api/branches").contentType("application/json").content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.code").exists());
    }
}
