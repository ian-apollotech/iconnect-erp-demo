// src/main/java/com/puregold/iconnect/product/ProductPackUom.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_pack_uom")
public class ProductPackUom extends BaseEntity {

    private String level;      // "Base" | "Inner" | "Case"
    private String uom;        // e.g. "PC", "PACK-6", "CASE-12"
    private Integer qtyInBase;
    private String barcode;
    private boolean orderable;
    private boolean sellable;

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public Integer getQtyInBase() { return qtyInBase; }
    public void setQtyInBase(Integer qtyInBase) { this.qtyInBase = qtyInBase; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public boolean isOrderable() { return orderable; }
    public void setOrderable(boolean orderable) { this.orderable = orderable; }
    public boolean isSellable() { return sellable; }
    public void setSellable(boolean sellable) { this.sellable = sellable; }
}
