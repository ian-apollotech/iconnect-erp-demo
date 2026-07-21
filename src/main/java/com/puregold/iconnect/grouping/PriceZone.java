// src/main/java/com/puregold/iconnect/grouping/PriceZone.java
package com.puregold.iconnect.grouping;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "price_zone")
public class PriceZone extends StoreGrouping {
}
