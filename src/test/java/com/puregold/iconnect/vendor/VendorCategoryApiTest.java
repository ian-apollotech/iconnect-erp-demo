// src/test/java/com/puregold/iconnect/vendor/VendorCategoryApiTest.java
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
class VendorCategoryApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

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
        Long vendorId = createVendor("Category Test Vendor");

        String l1Body = """
            {"vendorId":%d,"level":"Category (L1)","nodeName":"Grocery","nodeCode":"GRO",
             "allowanceEligible":"Yes","status":"Active"}
            """.formatted(vendorId);
        String l1Response = mvc.perform(post("/api/vendor-categories").contentType("application/json").content(l1Body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.nodeName").value("Grocery"))
           .andReturn().getResponse().getContentAsString();
        Long l1Id = mapper.readTree(l1Response).get("id").asLong();

        String l2Body = """
            {"vendorId":%d,"level":"Sub-Category (L2)","parentCategoryId":%d,"nodeName":"Canned Goods",
             "nodeCode":"GRO-CAN","allowanceEligible":"Yes","status":"Active"}
            """.formatted(vendorId, l1Id);
        mvc.perform(post("/api/vendor-categories").contentType("application/json").content(l2Body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.nodeName").value("Canned Goods"))
           .andExpect(jsonPath("$.parentCategoryId").value(l1Id));

        mvc.perform(get("/api/vendor-categories"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.nodeName=='Canned Goods')]").exists());
    }

    @Test
    void rejectsSubCategoryWithoutValidParent() throws Exception {
        Long vendorId = createVendor("Category Test Vendor 2");
        String body = """
            {"vendorId":%d,"level":"Sub-Category (L2)","nodeName":"Orphan","status":"Active"}
            """.formatted(vendorId);
        mvc.perform(post("/api/vendor-categories").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
