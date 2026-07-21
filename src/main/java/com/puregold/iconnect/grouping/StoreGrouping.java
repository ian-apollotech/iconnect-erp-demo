// src/main/java/com/puregold/iconnect/grouping/StoreGrouping.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class StoreGrouping extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(unique = true)
    private String code;
    @Column(nullable = false)
    private Long companyId;
    private String status = "Active";
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
