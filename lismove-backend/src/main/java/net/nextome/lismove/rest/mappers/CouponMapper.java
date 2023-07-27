package net.nextome.lismove.rest.mappers;

import net.nextome.lismove.models.Coupon;
import net.nextome.lismove.rest.dto.CouponDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper(componentModel = "spring")
@Service
public abstract class CouponMapper extends UtilMapper{

    @Mapping(target = "uid", source = "user.uid")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "shopName", source = "shop.name")
    @Mapping(target = "shopLogo", source = "shop.logo")
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleTitle", source = "article.title")
    @Mapping(target = "articleImage", source = "article.image")
    public abstract CouponDto couponToDto(Coupon coupon);
    public abstract List<CouponDto> couponToDto(List<Coupon> coupon);

}
