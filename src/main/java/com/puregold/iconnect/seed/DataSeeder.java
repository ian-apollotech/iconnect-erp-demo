// src/main/java/com/puregold/iconnect/seed/DataSeeder.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.branch.Branch;
import com.puregold.iconnect.branch.BranchRepository;
import com.puregold.iconnect.company.Company;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.grouping.AssortmentCluster;
import com.puregold.iconnect.grouping.AssortmentClusterRepository;
import com.puregold.iconnect.grouping.CostZone;
import com.puregold.iconnect.grouping.CostZoneRepository;
import com.puregold.iconnect.grouping.PriceZone;
import com.puregold.iconnect.grouping.PriceZoneRepository;
import com.puregold.iconnect.grouping.ReplenishmentProfile;
import com.puregold.iconnect.grouping.ReplenishmentProfileRepository;
import com.puregold.iconnect.product.MerchNode;
import com.puregold.iconnect.product.MerchNodeRepository;
import com.puregold.iconnect.product.Product;
import com.puregold.iconnect.product.ProductPackUom;
import com.puregold.iconnect.product.ProductRepository;
import com.puregold.iconnect.vendor.Vendor;
import com.puregold.iconnect.vendor.VendorAllowance;
import com.puregold.iconnect.vendor.VendorAllowanceRepository;
import com.puregold.iconnect.vendor.VendorCategory;
import com.puregold.iconnect.vendor.VendorCategoryRepository;
import com.puregold.iconnect.vendor.VendorRepository;
import com.puregold.iconnect.vendor.VendorSchedule;
import com.puregold.iconnect.vendor.VendorScheduleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companies;
    private final PriceZoneRepository priceZones;
    private final CostZoneRepository costZones;
    private final AssortmentClusterRepository clusters;
    private final ReplenishmentProfileRepository replenProfiles;
    private final VendorRepository vendors;
    private final VendorCategoryRepository vendorCategories;
    private final VendorScheduleRepository vendorSchedules;
    private final VendorAllowanceRepository vendorAllowances;
    private final BranchRepository branches;
    private final MerchNodeRepository merchNodes;
    private final ProductRepository products;

    // Filled in during seedGroupings(), keyed by grouping name, reused by seedBranches().
    private final Map<String, Long> priceZoneIdByName = new LinkedHashMap<>();
    private final Map<String, Long> costZoneIdByName = new LinkedHashMap<>();
    private final Map<String, Long> clusterIdByName = new LinkedHashMap<>();
    private final Map<String, Long> replenProfileIdByName = new LinkedHashMap<>();

    // Filled in during seedVendors(), keyed by vendor name, reused by categories/schedules/allowances.
    private final Map<String, Long> vendorIdByName = new LinkedHashMap<>();

    // Filled in during seedMerchNodes(), keyed by node code, reused by seedProducts().
    private final Map<String, Long> merchNodeIdByCode = new LinkedHashMap<>();

    public DataSeeder(CompanyRepository companies,
                       PriceZoneRepository priceZones,
                       CostZoneRepository costZones,
                       AssortmentClusterRepository clusters,
                       ReplenishmentProfileRepository replenProfiles,
                       VendorRepository vendors,
                       VendorCategoryRepository vendorCategories,
                       VendorScheduleRepository vendorSchedules,
                       VendorAllowanceRepository vendorAllowances,
                       BranchRepository branches,
                       MerchNodeRepository merchNodes,
                       ProductRepository products) {
        this.companies = companies;
        this.priceZones = priceZones;
        this.costZones = costZones;
        this.clusters = clusters;
        this.replenProfiles = replenProfiles;
        this.vendors = vendors;
        this.vendorCategories = vendorCategories;
        this.vendorSchedules = vendorSchedules;
        this.vendorAllowances = vendorAllowances;
        this.branches = branches;
        this.merchNodes = merchNodes;
        this.products = products;
    }

    @Override
    public void run(String... args) {
        seedCompanies();
        seedGroupings();
        seedVendors();
        seedVendorCategories();
        seedVendorSchedules();
        seedVendorAllowances();
        seedBranches();
        seedMerchNodes();
        seedProducts();
    }

    // ---------------------------------------------------------------- companies

    private void seedCompanies() {
        if (companies.count() > 0) return;
        companies.save(company("PGOLD", "Puregold Price Club, Inc.", "Puregold", "000-123-456-000"));
        companies.save(company("AYAG", "Ayagold Retailers Inc.", "Merkado", "000-987-654-000"));
    }

    private Company company(String code, String legalName, String trade, String tin) {
        Company c = new Company();
        c.setCode(code);
        c.setLegalName(legalName);
        c.setTradeName(trade);
        c.setTin(tin);
        c.setFiscalYearStart("January");
        c.setBaseCurrency("PHP");
        c.setDefaultVat("12% VATable");
        c.setPriceIncludesVat("Yes (VAT-inclusive)");
        c.setStatus("Active");
        return c;
    }

    // ---------------------------------------------------------------- groupings

    private void seedGroupings() {
        Long pgold = companies.findByCode("PGOLD").orElseThrow().getId();
        Long ayag = companies.findByCode("AYAG").orElseThrow().getId();

        if (priceZones.count() == 0) {
            for (String name : List.of("Price Club", "Extra", "Minimart", "Merkado", "Visayas", "NCR Metro")) {
                Long companyId = "Merkado".equals(name) ? ayag : pgold;
                PriceZone z = new PriceZone();
                z.setName(name);
                z.setCode(autoCode("PZ", name));
                z.setCompanyId(companyId);
                z.setStatus("Active");
                PriceZone saved = priceZones.save(z);
                priceZoneIdByName.put(name, saved.getId());
            }
        } else {
            priceZones.findAll().forEach(z -> priceZoneIdByName.put(z.getName(), z.getId()));
        }

        if (costZones.count() == 0) {
            for (String name : List.of("Price Club National", "Luzon Cost", "Visayas–Mindanao Cost", "Merkado Cost")) {
                Long companyId = name.startsWith("Merkado") ? ayag : pgold;
                CostZone z = new CostZone();
                z.setName(name);
                z.setCode(autoCode("CZ", name));
                z.setCompanyId(companyId);
                z.setStatus("Active");
                CostZone saved = costZones.save(z);
                costZoneIdByName.put(name, saved.getId());
            }
        } else {
            costZones.findAll().forEach(z -> costZoneIdByName.put(z.getName(), z.getId()));
        }

        if (clusters.count() == 0) {
            for (String name : List.of("Urban Large", "Urban Small", "Convenience", "Provincial Mixed")) {
                AssortmentCluster c = new AssortmentCluster();
                c.setName(name);
                c.setCode(autoCode("AC", name));
                c.setCompanyId(pgold);
                c.setStatus("Active");
                AssortmentCluster saved = clusters.save(c);
                clusterIdByName.put(name, saved.getId());
            }
        } else {
            clusters.findAll().forEach(c -> clusterIdByName.put(c.getName(), c.getId()));
        }

        if (replenProfiles.count() == 0) {
            for (String name : List.of("Metro Fast-Movers", "Provincial Weekly", "Minimart 2×/Week")) {
                ReplenishmentProfile p = new ReplenishmentProfile();
                p.setName(name);
                p.setCode(autoCode("RP", name));
                p.setCompanyId(pgold);
                p.setStatus("Active");
                ReplenishmentProfile saved = replenProfiles.save(p);
                replenProfileIdByName.put(name, saved.getId());
            }
        } else {
            replenProfiles.findAll().forEach(p -> replenProfileIdByName.put(p.getName(), p.getId()));
        }
    }

    private String autoCode(String prefix, String name) {
        String slug = name.toUpperCase()
            .replace("–", "-")
            .replaceAll("[^A-Z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        return prefix + "-" + slug;
    }

    // ---------------------------------------------------------------- vendors

    private void seedVendors() {
        if (vendors.count() > 0) {
            vendors.findAll().forEach(v -> vendorIdByName.put(v.getName(), v.getId()));
            return;
        }
        Vendor v1 = vendor("V-1004", "Alaska Milk Corporation", "Cross-dock", "30 days", "2 days", "Yes", "Active");
        Vendor v2 = vendor("V-1027", "Gardenia Bakeries PH", "DSD", "7 days", "Daily", "Yes", "Active");
        Vendor v3 = vendor("V-1033", "Selecta Ice Cream (RFM)", "DSD", "15 days", "Daily", "Yes", "Active");
        Vendor v4 = vendor("V-1140", "Nestlé Philippines", "Direct-to-Store (DTS)", "45 days", "3 days", "Yes", "Active");
        Vendor v5 = vendor("V-1208", "San Miguel Foods", "Direct-to-Store (DTS)", "30 days", "2 days", "No", "Active");
        Vendor v6 = vendor("V-1315", "Monde Nissin Corp.", "Cross-dock", "30 days", "4 days", "Yes", "On Hold");
        Vendor v7 = vendor("V-1422", "Universal Robina Corp.", "Direct-to-Store (DTS)", "30 days", "2 days", "No", "Active");

        for (Vendor v : List.of(v1, v2, v3, v4, v5, v6, v7)) {
            Vendor saved = vendors.save(v);
            vendorIdByName.put(saved.getName(), saved.getId());
        }
    }

    private Vendor vendor(String code, String name, String deliveryModel, String paymentTerms,
                           String leadTime, String rtvAllowed, String status) {
        Vendor v = new Vendor();
        v.setCode(code);
        v.setName(name);
        v.setDeliveryModel(deliveryModel);
        v.setPaymentTerms(paymentTerms);
        v.setLeadTime(leadTime);
        v.setRtvAllowed(rtvAllowed);
        v.setStatus(status);
        return v;
    }

    // ---------------------------------------------------------------- vendor categories

    private void seedVendorCategories() {
        if (vendorCategories.count() > 0) return;
        Long nestle = vendorIdByName.get("Nestlé Philippines");
        if (nestle == null) {
            throw new IllegalStateException("Cannot seed vendor categories: Nestlé Philippines vendor not found");
        }

        // L1 -> list of L2 sub-category names
        Map<String, List<String>> tree = new LinkedHashMap<>();
        tree.put("Beverages", List.of("Coffee", "Creamer", "Cereal Drinks"));
        tree.put("Dairy", List.of("Powdered Milk", "Condensed & Evaporated"));
        tree.put("Culinary", List.of("Sauces & Mixes", "Seasoning"));

        for (Map.Entry<String, List<String>> entry : tree.entrySet()) {
            VendorCategory l1 = new VendorCategory();
            l1.setVendorId(nestle);
            l1.setLevel("Category (L1)");
            l1.setNodeName(entry.getKey());
            l1.setStatus("Active");
            VendorCategory savedL1 = vendorCategories.save(l1);

            for (String subName : entry.getValue()) {
                VendorCategory l2 = new VendorCategory();
                l2.setVendorId(nestle);
                l2.setLevel("Sub-Category (L2)");
                l2.setParentCategoryId(savedL1.getId());
                l2.setNodeName(subName);
                l2.setStatus("Active");
                vendorCategories.save(l2);
            }
        }
    }

    // ---------------------------------------------------------------- vendor schedules

    private void seedVendorSchedules() {
        if (vendorSchedules.count() > 0) return;
        Long pgold = companies.findByCode("PGOLD").orElseThrow().getId();

        for (Vendor v : vendors.findAll()) {
            VendorSchedule s = new VendorSchedule();
            s.setCompanyId(pgold);
            s.setVendorId(v.getId());
            s.setOrderDays("Mon · Thu");
            s.setLeadTimeDays(parseLeadTimeDays(v.getLeadTime()));
            s.setStatus("Active");
            vendorSchedules.save(s);
        }
    }

    private Integer parseLeadTimeDays(String leadTime) {
        if (leadTime == null) return null;
        if ("Daily".equalsIgnoreCase(leadTime)) return 1;
        String digits = leadTime.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : Integer.parseInt(digits);
    }

    // ---------------------------------------------------------------- vendor allowances

    private void seedVendorAllowances() {
        if (vendorAllowances.count() > 0) return;

        allowance("AL-3301", "Nestlé Philippines", "Volume Rebate", "% of Purchase", "3.50%",
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Active");
        allowance("AL-3312", "Alaska Milk Corporation", "Promo Allowance", "Per Case", "₱ 12.00",
            LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31), "Active");
        allowance("AL-3320", "Universal Robina Corp.", "Display / Facing", "Fixed Amount", "₱ 250,000",
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 9, 30), "Active");
        allowance("AL-3334", "San Miguel Foods", "Listing Fee", "Fixed Amount", "₱ 80,000",
            LocalDate.of(2026, 7, 1), LocalDate.of(2027, 6, 30), "Pending");
        allowance("AL-3340", "Monde Nissin Corp.", "Markdown Support", "% of Purchase", "5.00%",
            LocalDate.of(2026, 5, 15), LocalDate.of(2026, 7, 31), "Expiring");
    }

    private void allowance(String ref, String vendorName, String type, String basis, String rateAmount,
                            LocalDate validFrom, LocalDate validTo, String status) {
        Long vendorId = vendorIdByName.get(vendorName);
        if (vendorId == null) {
            throw new IllegalStateException("Cannot seed allowance " + ref + ": vendor not found: " + vendorName);
        }
        VendorAllowance a = new VendorAllowance();
        a.setRef(ref);
        a.setVendorId(vendorId);
        a.setAllowanceType(type);
        a.setBasis(basis);
        a.setRateAmount(rateAmount);
        a.setValidFrom(validFrom);
        a.setValidTo(validTo);
        a.setStatus(status);
        vendorAllowances.save(a);
    }

    // ---------------------------------------------------------------- branches

    private void seedBranches() {
        if (branches.count() > 0) return;
        Long pgold = companies.findByCode("PGOLD").orElseThrow().getId();
        Long ayag = companies.findByCode("AYAG").orElseThrow().getId();

        // code, name, region
        String[][] rows = {
            {"0102", "Ortigas Ext.", "NCR"},
            {"0107", "Araneta Cubao", "NCR"},
            {"0233", "Mandaue (Cebu)", "Visayas"},
            {"0311", "Bajada (Davao)", "Mindanao"},
            {"0461", "Clark", "N.Luzon"},
            {"0119", "Jr. Baclaran", "NCR"},
            {"0450", "Jr. Baguio", "N.Luzon"},
            {"0146", "Extra Cainta", "S.Luzon"},
            {"0531", "Mini Kamias", "NCR"},
            {"AYA-03", "Merkado UP Town", "NCR"},
        };

        for (String[] row : rows) {
            String code = row[0];
            String name = row[1];
            String region = row[2];
            String format = formatFor(name);
            Long companyId = "Merkado".equals(format) ? ayag : pgold;

            Branch b = new Branch();
            b.setCode(code);
            b.setName(name);
            b.setFormat(format);
            b.setRegion(region);
            b.setCompanyId(companyId);
            b.setPriceZoneId(priceZoneIdByName.get(format));
            b.setCostZoneId(costZoneIdFor(format, region));
            b.setAssortmentClusterId(clusterIdFor(format, region));
            b.setReplenishmentProfileId(replenProfileIdFor(format, region));
            b.setStatus("Active");
            branches.save(b);
        }
    }

    private String formatFor(String name) {
        if (name.contains("Jr.") || name.contains("Extra")) return "Extra";
        if (name.contains("Mini")) return "Minimart";
        if (name.contains("Merkado")) return "Merkado";
        return "Price Club";
    }

    private Long costZoneIdFor(String format, String region) {
        if ("Merkado".equals(format)) return costZoneIdByName.get("Merkado Cost");
        if ("Visayas".equals(region) || "Mindanao".equals(region)) {
            return costZoneIdByName.get("Visayas–Mindanao Cost");
        }
        if ("N.Luzon".equals(region) || "S.Luzon".equals(region)) return costZoneIdByName.get("Luzon Cost");
        return costZoneIdByName.get("Price Club National");
    }

    private Long clusterIdFor(String format, String region) {
        if ("Minimart".equals(format) || "Merkado".equals(format)) return clusterIdByName.get("Convenience");
        if ("Extra".equals(format)) return clusterIdByName.get("Urban Small");
        if ("NCR".equals(region)) return clusterIdByName.get("Urban Large");
        return clusterIdByName.get("Provincial Mixed");
    }

    private Long replenProfileIdFor(String format, String region) {
        if ("Minimart".equals(format)) return replenProfileIdByName.get("Minimart 2×/Week");
        if ("NCR".equals(region)) return replenProfileIdByName.get("Metro Fast-Movers");
        return replenProfileIdByName.get("Provincial Weekly");
    }

    // ---------------------------------------------------------------- merch hierarchy

    private void seedMerchNodes() {
        if (merchNodes.count() > 0) {
            merchNodes.findAll().forEach(n -> merchNodeIdByCode.put(n.getCode(), n.getId()));
            return;
        }

        // Departments (L1)
        Long dept01 = node("Department (L1)", null, "01", "Grocery Food").getId();
        node("Department (L1)", null, "02", "Fresh & Perishables");
        node("Department (L1)", null, "03", "Non-Food & Household");
        node("Department (L1)", null, "04", "Fashion & Apparel");

        // Sub-Departments (L2) under 01 · Grocery Food
        Long subDept0110 = node("Sub-Department (L2)", dept01, "0110", "Dairy & Chilled").getId();
        node("Sub-Department (L2)", dept01, "0120", "Bakery");
        node("Sub-Department (L2)", dept01, "0130", "Beverages");

        // Classes (L3) under 0110 · Dairy & Chilled
        Long cls011001 = node("Class (L3)", subDept0110, "0110-01", "Milk").getId();
        node("Class (L3)", subDept0110, "0110-02", "Cheese");
        node("Class (L3)", subDept0110, "0110-03", "Yogurt");

        // Sub-Classes (L4) under 0110-01 · Milk — seeded directly via repository (not
        // service.create) so the Class-level auto-scaffold rule isn't re-triggered.
        node("Sub-Class (L4)", cls011001, "0110-01-101", "Fresh Milk");
        node("Sub-Class (L4)", cls011001, "0110-01-102", "UHT Milk");
        node("Sub-Class (L4)", cls011001, "0110-01-103", "Powdered Milk");
    }

    private MerchNode node(String level, Long parentId, String code, String name) {
        MerchNode n = new MerchNode();
        n.setLevel(level);
        n.setParentId(parentId);
        n.setCode(code);
        n.setName(name);
        n.setStatus("Active");
        MerchNode saved = merchNodes.save(n);
        merchNodeIdByCode.put(code, saved.getId());
        return saved;
    }

    // ---------------------------------------------------------------- products

    private void seedProducts() {
        if (products.count() > 0) return;

        // Only Alaska Fresh Milk maps to a seeded Sub-Class node (0110-01-101 · Fresh Milk) —
        // the other items' categories (Bakery, Coffee, Ice Cream, Eggs, Noodles, Baking, Frozen)
        // have no corresponding Sub-Class seeded in seedMerchNodes(), so they're left unclassified.
        Long freshMilk = merchNodeIdByCode.get("0110-01-101");

        Product alaska = product("100482", "Alaska Fresh Milk 1L", "4800016641107",
            "ALASKA F.MILK 1L", "Alaska", freshMilk, "58.20", "72.00");
        product("100517", "Gardenia White Bread 600g", "4800092770019",
            "GARDENIA BREAD 600G", "Gardenia", null, "46.50", "59.75");
        product("100630", "Nescafé Classic 100g", "4800361001236",
            "NESCAFE CLASSIC 100G", "Nestlé", null, "118.00", "149.75");
        product("100744", "Selecta Cornetto Choco 110ml", "4800016114009",
            "SELECTA CORNETTO CHOC", "Selecta", null, "22.80", "32.00");
        product("100901", "Fresh Eggs Medium Tray 30s", "4806507050014",
            "FRESH EGGS MED TRAY30", "San Miguel", null, "196.00", "238.00");
        product("101120", "Lucky Me Pancit Canton 60g", "4800024570017",
            "LUCKY ME PANCIT CANT", "Monde Nissin", null, "9.20", "13.50");
        product("900014", "Pure Basics All-Purpose Flour 1kg", "4809014000148",
            "PB FLOUR 1KG", "Pure Basics (Own)", null, "44.00", "55.00");
        product("900027", "Nature’s Peak Mixed Vegetables 400g", "4809014000278",
            "NP MIXED VEG 400G", "Pure Basics (Own)", null, "52.00", "68.00");

        // Pack & UOM for Alaska Fresh Milk (from mockup Pack & UOM tab)
        addPackUom(alaska, "Base", "PC", 1, "4800016641107", false, true);
        addPackUom(alaska, "Inner", "PACK-6", 6, "14800016641104", true, false);
        addPackUom(alaska, "Case", "CASE-12", 12, "24800016641101", true, false);
        products.save(alaska);
    }

    private Product product(String code, String description, String barcode, String shortName,
                             String brand, Long merchNodeId, String cost, String retail) {
        Product p = new Product();
        p.setCode(code);
        p.setDescription(description);
        p.setPrimaryBarcode(barcode);
        p.setShortName(shortName);
        p.setBrand(brand);
        p.setMerchNodeId(merchNodeId);
        p.setStandardCost(new BigDecimal(cost));
        p.setBaseRetail(new BigDecimal(retail));
        p.setStatus("Active");
        return products.save(p);
    }

    private void addPackUom(Product product, String level, String uom, int qtyInBase,
                             String barcode, boolean orderable, boolean sellable) {
        ProductPackUom pu = new ProductPackUom();
        pu.setLevel(level);
        pu.setUom(uom);
        pu.setQtyInBase(qtyInBase);
        pu.setBarcode(barcode);
        pu.setOrderable(orderable);
        pu.setSellable(sellable);
        product.getPackUoms().add(pu);
    }
}
