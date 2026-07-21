// src/main/java/com/puregold/iconnect/grouping/AssortmentPlanLine.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "assortment_plan_line")
public class AssortmentPlanLine extends BaseEntity {
    private Long productId;
    private String productCode;
    private String description;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
