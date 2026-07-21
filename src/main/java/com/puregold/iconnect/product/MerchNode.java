// src/main/java/com/puregold/iconnect/product/MerchNode.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "merch_node")
public class MerchNode extends BaseEntity {
    private String level;               // "Department (L1)" | "Sub-Department (L2)" | "Class (L3)" | "Sub-Class (L4)"
    private Long parentId;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    private String merchManager;
    private String replenStrategy;
    private String perishable;
    private String shelfLife;
    private String otbPlanned;
    private String status = "Active";

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMerchManager() { return merchManager; }
    public void setMerchManager(String merchManager) { this.merchManager = merchManager; }
    public String getReplenStrategy() { return replenStrategy; }
    public void setReplenStrategy(String replenStrategy) { this.replenStrategy = replenStrategy; }
    public String getPerishable() { return perishable; }
    public void setPerishable(String perishable) { this.perishable = perishable; }
    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }
    public String getOtbPlanned() { return otbPlanned; }
    public void setOtbPlanned(String otbPlanned) { this.otbPlanned = otbPlanned; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
