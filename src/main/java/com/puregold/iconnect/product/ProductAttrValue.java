// src/main/java/com/puregold/iconnect/product/ProductAttrValue.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_attr_value")
public class ProductAttrValue extends BaseEntity {

    private String attributeName;
    @Column(name = "attr_value")
    private String value;

    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
