// src/test/java/com/puregold/iconnect/vendor/VendorAllowanceApiTest.java
package com.puregold.iconnect.vendor;

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
class VendorAllowanceApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createVendor(String name) throws Exception {
        String body = """
            {"name":"%s","rtvAllowed":"Yes","status":"Active"}
            """.formatted(name);
        String response = mvc.perform(post("/backend/vendors").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    @Test
    void createThenList() throws Exception {
        Long vendorId = createVendor("Allowance Test Vendor");

        String body = """
            {"vendorId":%d,"allowanceType":"Volume Rebate","basis":"%% of Purchase",
             "rateAmount":"3.50%%","validFrom":"2026-01-01","validTo":"2026-12-31","status":"Active"}
            """.formatted(vendorId);

        mvc.perform(post("/backend/vendor-allowances").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.allowanceType").value("Volume Rebate"))
           .andExpect(jsonPath("$.rateAmount").value("3.50%"))
           .andExpect(jsonPath("$.ref").value(org.hamcrest.Matchers.startsWith("AL-")));

        mvc.perform(get("/backend/vendor-allowances"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.allowanceType=='Volume Rebate')]").exists());
    }

    @Test
    void rejectsNonExistentVendor() throws Exception {
        String body = """
            {"vendorId":999999,"allowanceType":"Volume Rebate","basis":"% of Purchase","rateAmount":"3.50%"}
            """;
        mvc.perform(post("/backend/vendor-allowances").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
