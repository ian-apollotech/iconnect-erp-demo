// src/main/java/com/puregold/iconnect/product/ProductController.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController
        extends AbstractCrudController<Product, ProductDtos.Request, ProductDtos.Response> {

    public ProductController(ProductService service) { super(service); }

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
    }

    @Override
    protected ProductDtos.Response toResponse(Product p) {
        List<ProductDtos.PackUomResponse> packUoms = new ArrayList<>();
        for (ProductPackUom pu : p.getPackUoms()) {
            packUoms.add(new ProductDtos.PackUomResponse(
                pu.getId(), pu.getLevel(), pu.getUom(), pu.getQtyInBase(),
                pu.getBarcode(), pu.isOrderable(), pu.isSellable()));
        }
        return new ProductDtos.Response(
            p.getId(), p.getCode(), p.getDescription(), p.getPrimaryBarcode(), p.getShortName(),
            p.getBrand(), p.getMerchNodeId(), p.getPerishable(), p.getShelfLife(), p.getVatClass(),
            p.getListingFee(), p.getStoreRange(), p.getBuyingUom(), p.getSellingUom(),
            p.getStandardCost(), p.getBaseRetail(), marginPct(p.getStandardCost(), p.getBaseRetail()),
            null /* preferredVendor — filled from VendorProduct in Task 11 */,
            p.getStatus(), packUoms);
    }

    private BigDecimal marginPct(BigDecimal cost, BigDecimal retail) {
        if (cost == null || retail == null || retail.signum() == 0) return null;
        return retail.subtract(cost)
            .divide(retail, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(1, RoundingMode.HALF_UP);
    }
}
