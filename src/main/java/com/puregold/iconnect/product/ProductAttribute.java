// src/main/java/com/puregold/iconnect/product/ProductAttribute.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attribute")
public class ProductAttribute extends BaseEntity {
    @Column(unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 1000)
    private String allowedValues;
    private String status = "Active";
    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAllowedValues() { return allowedValues; }
    public void setAllowedValues(String allowedValues) { this.allowedValues = allowedValues; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
