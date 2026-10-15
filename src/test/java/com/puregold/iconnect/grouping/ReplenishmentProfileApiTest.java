// src/test/java/com/puregold/iconnect/grouping/ReplenishmentProfileApiTest.java
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
class ReplenishmentProfileApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createCompany(String code) throws Exception {
        String body = """
            {"code":"%s","legalName":"ReplenProfile Test Co","baseCurrency":"PHP","status":"Active"}
            """.formatted(code);
        String response = mvc.perform(post("/backend/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void createThenList() throws Exception {
        Long companyId = createCompany("RPTESTCO");
        String body = """
            {"name":"Fast-Mover Profile","code":"RP001","companyId":%d,"status":"Active","description":"High velocity SKUs"}
            """.formatted(companyId);

        mvc.perform(post("/backend/replenishment-profiles").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.code").value("RP001"))
           .andExpect(jsonPath("$.id").isNumber())
           .andExpect(jsonPath("$.companyId").value(companyId))
           .andExpect(jsonPath("$.storeCount").value(0));

        mvc.perform(get("/backend/replenishment-profiles"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='RP001')]").exists());
    }

    @Test
    void rejectsNonExistentCompany() throws Exception {
        String body = """
            {"name":"Bad Profile","code":"RP002","companyId":999999}
            """;
        mvc.perform(post("/backend/replenishment-profiles").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
