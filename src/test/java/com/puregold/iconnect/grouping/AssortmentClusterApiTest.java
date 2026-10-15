// src/test/java/com/puregold/iconnect/grouping/AssortmentClusterApiTest.java
package com.puregold.iconnect.grouping;

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
class AssortmentClusterApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createCompany(String code) throws Exception {
        String body = """
            {"code":"%s","legalName":"AssortCluster Test Co","baseCurrency":"PHP","status":"Active"}
            """.formatted(code);
        String response = mvc.perform(post("/backend/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void createThenList() throws Exception {
        Long companyId = createCompany("ACTESTCO");
        String body = """
            {"name":"Urban Cluster","code":"AC001","companyId":%d,"status":"Active",
             "description":"Metro urban stores","storeProfile":"Large Format","tradeArea":"Urban"}
            """.formatted(companyId);

        mvc.perform(post("/backend/assortment-clusters").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.code").value("AC001"))
           .andExpect(jsonPath("$.id").isNumber())
           .andExpect(jsonPath("$.companyId").value(companyId))
           .andExpect(jsonPath("$.tradeArea").value("Urban"))
           .andExpect(jsonPath("$.storeCount").value(0));

        mvc.perform(get("/backend/assortment-clusters"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='AC001')]").exists());
    }

    @Test
    void rejectsNonExistentCompany() throws Exception {
        String body = """
            {"name":"Bad Cluster","code":"AC002","companyId":999999}
            """;
        mvc.perform(post("/backend/assortment-clusters").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
