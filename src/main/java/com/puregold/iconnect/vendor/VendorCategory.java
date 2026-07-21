// src/main/java/com/puregold/iconnect/vendor/VendorCategory.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor_category")
public class VendorCategory extends BaseEntity {
    @Column(nullable = false)
    private Long vendorId;
    private String level;               // "Category (L1)" | "Sub-Category (L2)"
    private Long parentCategoryId;
    @Column(nullable = false)
    private String nodeName;
    private String nodeCode;
    private String allowanceEligible;   // "Yes" | "No"
    private String status = "Active";

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public Long getParentCategoryId() { return parentCategoryId; }
    public void setParentCategoryId(Long parentCategoryId) { this.parentCategoryId = parentCategoryId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getNodeCode() { return nodeCode; }
    public void setNodeCode(String nodeCode) { this.nodeCode = nodeCode; }
    public String getAllowanceEligible() { return allowanceEligible; }
    public void setAllowanceEligible(String allowanceEligible) { this.allowanceEligible = allowanceEligible; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
