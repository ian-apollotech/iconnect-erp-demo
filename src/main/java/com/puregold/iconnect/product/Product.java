// src/main/java/com/puregold/iconnect/product/Product.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Column(unique = true)
    private String code;
    @Column(nullable = false)
    private String description;
    private String primaryBarcode;
    private String shortName;
    private String brand;
    private Long merchNodeId;
    private String perishable;
    private String shelfLife;
    private String vatClass;
    private String listingFee;      // "Yes" | "No"
    private String storeRange;
    private String buyingUom;
    private String sellingUom;
    private BigDecimal standardCost;
    private BigDecimal baseRetail;
    private String status = "Active";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @OrderColumn(name = "pack_uom_order")
    private List<ProductPackUom> packUoms = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    @OrderColumn(name = "attr_value_order")
    private List<ProductAttrValue> attributes = new ArrayList<>();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPrimaryBarcode() { return primaryBarcode; }
    public void setPrimaryBarcode(String primaryBarcode) { this.primaryBarcode = primaryBarcode; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public Long getMerchNodeId() { return merchNodeId; }
    public void setMerchNodeId(Long merchNodeId) { this.merchNodeId = merchNodeId; }
    public String getPerishable() { return perishable; }
    public void setPerishable(String perishable) { this.perishable = perishable; }
    public String getShelfLife() { return shelfLife; }
    public void setShelfLife(String shelfLife) { this.shelfLife = shelfLife; }
    public String getVatClass() { return vatClass; }
    public void setVatClass(String vatClass) { this.vatClass = vatClass; }
    public String getListingFee() { return listingFee; }
    public void setListingFee(String listingFee) { this.listingFee = listingFee; }
    public String getStoreRange() { return storeRange; }
    public void setStoreRange(String storeRange) { this.storeRange = storeRange; }
    public String getBuyingUom() { return buyingUom; }
    public void setBuyingUom(String buyingUom) { this.buyingUom = buyingUom; }
    public String getSellingUom() { return sellingUom; }
    public void setSellingUom(String sellingUom) { this.sellingUom = sellingUom; }
    public BigDecimal getStandardCost() { return standardCost; }
    public void setStandardCost(BigDecimal standardCost) { this.standardCost = standardCost; }
    public BigDecimal getBaseRetail() { return baseRetail; }
    public void setBaseRetail(BigDecimal baseRetail) { this.baseRetail = baseRetail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<ProductPackUom> getPackUoms() { return packUoms; }
    public void setPackUoms(List<ProductPackUom> packUoms) { this.packUoms = packUoms; }
    public List<ProductAttrValue> getAttributes() { return attributes; }
    public void setAttributes(List<ProductAttrValue> attributes) { this.attributes = attributes; }
}
