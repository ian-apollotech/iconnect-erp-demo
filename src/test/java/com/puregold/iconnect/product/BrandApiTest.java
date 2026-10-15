// src/test/java/com/puregold/iconnect/product/BrandApiTest.java
package com.puregold.iconnect.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.puregold.iconnect.vendor.Vendor;
import com.puregold.iconnect.vendor.VendorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BrandApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired VendorRepository vendorRepository;

    private Long createManufacturer() {
        Vendor v = new Vendor();
        v.setCode("V-TEST1-" + System.nanoTime());
        v.setName("Test Mfr");
        v.setStatus("Active");
        return vendorRepository.save(v).getId();
    }

    @Test
    void createThenList() throws Exception {
        Long mfrId = createManufacturer();
        String body = """
            {"name":"Test Brand","code":"BR-TEST","manufacturerId":%d,"description":"x"}
            """.formatted(mfrId);

        mvc.perform(post("/backend/brands").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.manufacturerId").value(mfrId))
           .andExpect(jsonPath("$.manufacturerName").value("Test Mfr"))
           .andExpect(jsonPath("$.productCount").isNumber());

        mvc.perform(get("/backend/brands"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='BR-TEST')]").exists());
    }

    @Test
    void rejectsNonExistentManufacturer() throws Exception {
        String body = """
            {"name":"Bad Brand","code":"BR-BAD","manufacturerId":999999}
            """;
        mvc.perform(post("/backend/brands").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicateCode() throws Exception {
        Long mfrId = createManufacturer();
        String body = """
            {"name":"Dup Brand","code":"BR-DUP","manufacturerId":%d}
            """.formatted(mfrId);

        mvc.perform(post("/backend/brands").contentType("application/json").content(body))
           .andExpect(status().isCreated());

        mvc.perform(post("/backend/brands").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
