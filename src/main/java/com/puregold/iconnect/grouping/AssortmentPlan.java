// src/main/java/com/puregold/iconnect/grouping/AssortmentPlan.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assortment_plan")
public class AssortmentPlan extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long clusterId;
    private Long companyId;
    private LocalDate effectiveFrom;
    private String status = "Active";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "assortment_plan_id")
    @OrderColumn(name = "line_order")
    private List<AssortmentPlanLine> lines = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getClusterId() { return clusterId; }
    public void setClusterId(Long clusterId) { this.clusterId = clusterId; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<AssortmentPlanLine> getLines() { return lines; }
    public void setLines(List<AssortmentPlanLine> lines) { this.lines = lines; }
}
