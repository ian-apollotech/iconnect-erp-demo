// src/test/java/com/puregold/iconnect/replenishment/ReplenishmentPolicyApiTest.java
package com.puregold.iconnect.replenishment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puregold.iconnect.grouping.ReplenishmentProfile;
import com.puregold.iconnect.grouping.ReplenishmentProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReplenishmentPolicyApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ReplenishmentProfileRepository replenishmentProfiles;

    private Long createProfile() {
        ReplenishmentProfile p = new ReplenishmentProfile();
        p.setName("API Test Profile");
        p.setCode("RP-TEST" + System.nanoTime());
        p.setCompanyId(1L);
        p.setStatus("Active");
        return replenishmentProfiles.save(p).getId();
    }

    @Test
    void createThenList() throws Exception {
        Long profileId = createProfile();
        String body = """
            {"profileId":%d,"itemScopeType":"Specific SKU","scopeRef":"SKU X","method":"Days-of-Supply","target":"DOS 7","safetyStockDays":5,"serviceLevel":"97.5%%"}
            """.formatted(profileId);

        mvc.perform(post("/backend/replenishment-policies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.profileId").value(profileId))
           .andExpect(jsonPath("$.profileName").isString())
           .andExpect(jsonPath("$.method").value("Days-of-Supply"));

        mvc.perform(get("/backend/replenishment-policies"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void rejectsNonExistentProfile() throws Exception {
        String body = """
            {"profileId":999999,"itemScopeType":"Specific SKU","scopeRef":"SKU X","method":"Days-of-Supply"}
            """;
        mvc.perform(post("/backend/replenishment-policies").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
