// src/main/java/com/puregold/iconnect/product/ProductController.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudController;
import com.puregold.iconnect.vendor.Vendor;
import com.puregold.iconnect.vendor.VendorProduct;
import com.puregold.iconnect.vendor.VendorProductRepository;
import com.puregold.iconnect.vendor.VendorRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/backend/products")
public class ProductController
        extends AbstractCrudController<Product, ProductDtos.Request, ProductDtos.Response> {

    private final VendorProductRepository vendorProducts;
    private final VendorRepository vendors;

    public ProductController(ProductService service, VendorProductRepository vendorProducts,
                              VendorRepository vendors) {
        super(service);
        this.vendorProducts = vendorProducts;
        this.vendors = vendors;
    }

    @Override
    protected Product toEntity(ProductDtos.Request req) {
        Product p = new Product();
        apply(req, p);
        return p;
    }

    @Override
    protected void apply(ProductDtos.Request req, Product p) {
        p.setCode(req.code());
        p.setDescription(req.description());
        p.setPrimaryBarcode(req.primaryBarcode());
        p.setShortName(req.shortName());
        p.setBrand(req.brand());
        p.setMerchNodeId(req.merchNodeId());
        p.setPerishable(req.perishable());
        p.setShelfLife(req.shelfLife());
        p.setVatClass(req.vatClass());
        p.setListingFee(req.listingFee());
        p.setStoreRange(req.storeRange());
        p.setBuyingUom(req.buyingUom());
        p.setSellingUom(req.sellingUom());
        p.setStandardCost(req.standardCost());
        p.setBaseRetail(req.baseRetail());
        if (req.status() != null) p.setStatus(req.status());

        if (req.packUoms() != null) {
            p.getPackUoms().clear();
            for (ProductDtos.PackUomRequest pu : req.packUoms()) {
                ProductPackUom entity = new ProductPackUom();
                entity.setLevel(pu.level());
                entity.setUom(pu.uom());
                entity.setQtyInBase(pu.qtyInBase());
                entity.setBarcode(pu.barcode());
                entity.setOrderable(pu.orderable());
                entity.setSellable(pu.sellable());
                p.getPackUoms().add(entity);
            }
        }

        if (req.attributes() != null) {
            p.getAttributes().clear();
            for (ProductDtos.AttrValue av : req.attributes()) {
                ProductAttrValue entity = new ProductAttrValue();
                entity.setAttributeName(av.name());
                entity.setValue(av.value());
                p.getAttributes().add(entity);
            }
        }
    }

    @Override
    protected ProductDtos.Response toResponse(Product p) {
        List<ProductDtos.PackUomResponse> packUoms = new ArrayList<>();
        for (ProductPackUom pu : p.getPackUoms()) {
            packUoms.add(new ProductDtos.PackUomResponse(
                pu.getId(), pu.getLevel(), pu.getUom(), pu.getQtyInBase(),
                pu.getBarcode(), pu.isOrderable(), pu.isSellable()));
        }
        List<ProductDtos.AttrValue> attributes = new ArrayList<>();
        for (ProductAttrValue av : p.getAttributes()) {
            attributes.add(new ProductDtos.AttrValue(av.getAttributeName(), av.getValue()));
        }
        return new ProductDtos.Response(
            p.getId(), p.getCode(), p.getDescription(), p.getPrimaryBarcode(), p.getShortName(),
            p.getBrand(), p.getMerchNodeId(), p.getPerishable(), p.getShelfLife(), p.getVatClass(),
            p.getListingFee(), p.getStoreRange(), p.getBuyingUom(), p.getSellingUom(),
            p.getStandardCost(), p.getBaseRetail(), marginPct(p.getStandardCost(), p.getBaseRetail()),
            preferredVendor(p.getId()),
            p.getStatus(), packUoms, attributes);
    }

    private BigDecimal marginPct(BigDecimal cost, BigDecimal retail) {
        if (cost == null || retail == null || retail.signum() == 0) return null;
        return retail.subtract(cost)
            .divide(retail, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(1, RoundingMode.HALF_UP);
    }

    private String preferredVendor(Long productId) {
        if (productId == null) return "—";
        Optional<VendorProduct> primary = vendorProducts.findByProductIdAndSourcePriority(productId, "Primary");
        if (primary.isEmpty()) return "—";
        return vendors.findById(primary.get().getVendorId()).map(Vendor::getName).orElse("—");
    }
}
