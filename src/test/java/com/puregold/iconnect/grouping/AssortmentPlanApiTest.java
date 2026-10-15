// src/test/java/com/puregold/iconnect/grouping/AssortmentPlanApiTest.java
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
class AssortmentPlanApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createCompany(String code) throws Exception {
        String body = """
            {"code":"%s","legalName":"AssortPlan Test Co","baseCurrency":"PHP","status":"Active"}
            """.formatted(code);
        String response = mvc.perform(post("/backend/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private Long createCluster(Long companyId, String code) throws Exception {
        String body = """
            {"name":"AssortPlan Test Cluster","code":"%s","companyId":%d,"status":"Active"}
            """.formatted(code, companyId);
        String response = mvc.perform(post("/backend/assortment-clusters").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    @Test
    void createWithLinesThenList() throws Exception {
        Long companyId = createCompany("APTESTCO");
        Long clusterId = createCluster(companyId, "APTEST-CL");

        String body = """
            {"name":"Test Plan","clusterId":%d,"companyId":%d,"effectiveFrom":"2026-08-01","status":"Active",
             "lines":[{"productId":1,"productCode":"100482","description":"Alaska Fresh Milk 1L"},
                       {"productId":2,"productCode":"100517","description":"Gardenia White Bread 600g"}]}
            """.formatted(clusterId, companyId);

        String response = mvc.perform(post("/backend/assortment-plans").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.name").value("Test Plan"))
           .andExpect(jsonPath("$.clusterId").value(clusterId))
           .andExpect(jsonPath("$.skuCount").value(2))
           .andExpect(jsonPath("$.lines.length()").value(2))
           .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(response);
        Long planId = node.get("id").asLong();

        mvc.perform(get("/backend/assortment-plans"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.id==" + planId + ")]").exists());
    }

    @Test
    void rejectsNonExistentCluster() throws Exception {
        String body = """
            {"name":"Bad Plan","clusterId":999999}
            """;
        mvc.perform(post("/backend/assortment-plans").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonExistentCompany() throws Exception {
        Long companyId = createCompany("APTESTCO2");
        Long clusterId = createCluster(companyId, "APTEST-CL2");

        String body = """
            {"name":"Bad Company Plan","clusterId":%d,"companyId":999999,"lines":[]}
            """.formatted(clusterId);

        mvc.perform(post("/backend/assortment-plans").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
