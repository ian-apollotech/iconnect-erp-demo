// src/test/java/com/puregold/iconnect/replenishment/OrderCalendarExceptionApiTest.java
package com.puregold.iconnect.replenishment;

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
class OrderCalendarExceptionApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private Long createCompany(String code) throws Exception {
        String body = """
            {"code":"%s","legalName":"OrderCalException Test Co","baseCurrency":"PHP","status":"Active"}
            """.formatted(code);
        String response = mvc.perform(post("/backend/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void createCompanyHoliday() throws Exception {
        Long companyId = createCompany("OCETESTCO");
        String body = """
            {"scope":"Company Holiday","companyId":%d,"date":"2026-08-21","type":"Non-order","label":"Ninoy Aquino Day"}
            """.formatted(companyId);

        mvc.perform(post("/backend/order-calendar-exceptions").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.companyCode").isString())
           .andExpect(jsonPath("$.date").value("2026-08-21"));

        mvc.perform(get("/backend/order-calendar-exceptions"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void rejectsVendorBlackoutWithoutVendor() throws Exception {
        Long companyId = createCompany("OCETESTCO2");
        String body = """
            {"scope":"Vendor Blackout","companyId":%d,"date":"2026-08-21","type":"Both"}
            """.formatted(companyId);

        mvc.perform(post("/backend/order-calendar-exceptions").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonExistentCompany() throws Exception {
        String body = """
            {"scope":"Company Holiday","companyId":999999,"date":"2026-08-21","type":"Non-order"}
            """;
        mvc.perform(post("/backend/order-calendar-exceptions").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
