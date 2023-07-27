package net.nextome.lismove.rest.mappers;

import com.google.gson.Gson;
import net.nextome.lismove.models.*;
import net.nextome.lismove.rest.dto.ArticleDto;
import net.nextome.lismove.rest.dto.ShopDto;
import net.nextome.lismove.rest.dto.VendorDto;
import net.nextome.lismove.services.ShopService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
@Service
public abstract class VendorMapper extends UtilMapper {
    Gson gson = new Gson();

    @Autowired
    private ShopService shopService;

    //vendor dto è un insieme di dati del vendor e dell'utente
    public abstract Vendor dtoToVendor(VendorDto dto);

    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "iban", ignore = true)
    public abstract User dtoToUser(VendorDto dto);

    // --- --- --- vendor to dto --- --- ---
    @AfterMapping
    protected void setEnableCoupon(Vendor vendor, @MappingTarget VendorDto dto) {
        //posso riscattare coupon solo se esistono articoli per quel vendor
        if (vendor.getShops() != null) {
            dto.setEnableCoupon(shopService.existsArticles(vendor.getShops()));
        }
    }

    @Mapping(target = "uid", source = "user.uid")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "phone", source = "user.phoneNumber")
    @Mapping(target = "iban", source = "vendor.iban")
    public abstract VendorDto vendorToDto(Vendor vendor);

    public abstract List<VendorDto> vendorToDto(List<Vendor> vendor);
    //--- --- --- -- ---

    //------------------dtoToShop--------------
    @AfterMapping
    protected void dtoToShop(ShopDto dto, @MappingTarget Shop shop) {
        if (dto.getImages() != null)
            shop.setImages(gson.toJson(dto.getImages().toArray(), String[].class));
    }

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "ecommerce", source = "isEcommerce")
    @Mapping(target = "primary", source = "isPrimary")
    public abstract Shop dtoToShop(ShopDto dto);
    //----------------------------------------

    //--------------shopToDto--------------------------
    @AfterMapping
    protected void shopToDto(Shop shop, @MappingTarget ShopDto dto) {
        if (shop.getImages() != null)
            dto.setImages(new HashSet<>(Arrays.asList(gson.fromJson(shop.getImages(), String[].class))));
        //l'address sara' composito come address + number + city (provincia)
        if(shop.getAddress() != null ){
            dto.setAddress(shop.getAddress() + (shop.getNumber() != null ? " " + shop.getNumber()  : "") + (shop.getCity() != null? " " + shop.getCity().getCity() : "") + (shop.getCity() != null? " (" + shop.getCity().getProvince() +")" : ""));
        }
    }

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "isEcommerce", source = "ecommerce")
    @Mapping(target = "isPrimary", source = "primary")
    public abstract ShopDto shopToDto(Shop shop);

    public abstract List<ShopDto> shopToDto(List<Shop> shops);
    //----------------------------------------

    //------------articleToDto------------------
    public abstract ArticleDto articleToDto(Article article);

    public abstract List<ArticleDto> articleToDto(List<Article> article);
    //----------------------------------------

    //------------dtoToArticle------------------
    public abstract Article dtoToArticle(ArticleDto dto);
    //----------------------------------------

}
