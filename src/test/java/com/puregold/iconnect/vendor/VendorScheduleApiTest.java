// src/test/java/com/puregold/iconnect/vendor/VendorScheduleApiTest.java
package com.puregold.iconnect.vendor;

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
class VendorScheduleApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createCompany(String code) throws Exception {
        String body = """
            {"code":"%s","legalName":"Schedule Test Co","baseCurrency":"PHP","status":"Active"}
            """.formatted(code);
        String response = mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    private Long createVendor(String name) throws Exception {
        String body = """
            {"name":"%s","rtvAllowed":"Yes","status":"Active"}
            """.formatted(name);
        String response = mvc.perform(post("/api/vendors").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("id").asLong();
    }

    @Test
    void createThenList() throws Exception {
        Long companyId = createCompany("SCHTESTCO");
        Long vendorId = createVendor("Schedule Test Vendor");

        String body = """
            {"companyId":%d,"vendorId":%d,"orderDays":"Mon · Thu","leadTimeDays":3,
             "deliveryDays":"Wed · Sat","status":"Active"}
            """.formatted(companyId, vendorId);

        mvc.perform(post("/api/vendor-schedules").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.orderDays").value("Mon · Thu"))
           .andExpect(jsonPath("$.id").isNumber());

        mvc.perform(get("/api/vendor-schedules"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.orderDays=='Mon · Thu')]").exists());
    }

    @Test
    void rejectsNonExistentVendor() throws Exception {
        Long companyId = createCompany("SCHTESTCO2");
        String body = """
            {"companyId":%d,"vendorId":999999,"orderDays":"Mon","leadTimeDays":3}
            """.formatted(companyId);
        mvc.perform(post("/api/vendor-schedules").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
