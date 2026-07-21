// src/main/java/com/puregold/iconnect/grouping/AssortmentCluster.java
package com.puregold.iconnect.grouping;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "assortment_cluster")
public class AssortmentCluster extends StoreGrouping {
    private String storeProfile;
    private String tradeArea;   // "Urban" | "Suburban" | "Provincial" | "Transit / Mall"

    public String getStoreProfile() { return storeProfile; }
    public void setStoreProfile(String storeProfile) { this.storeProfile = storeProfile; }
    public String getTradeArea() { return tradeArea; }
    public void setTradeArea(String tradeArea) { this.tradeArea = tradeArea; }
}
