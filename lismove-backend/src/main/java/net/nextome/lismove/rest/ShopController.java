package net.nextome.lismove.rest;

import io.swagger.annotations.ApiOperation;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.Article;
import net.nextome.lismove.models.Coupon;
import net.nextome.lismove.models.Shop;
import net.nextome.lismove.models.Vendor;
import net.nextome.lismove.rest.dto.ArticleDto;
import net.nextome.lismove.rest.dto.CouponDto;
import net.nextome.lismove.rest.mappers.CouponMapper;
import net.nextome.lismove.rest.mappers.VendorMapper;
import net.nextome.lismove.services.CouponService;
import net.nextome.lismove.services.ShopService;
import net.nextome.lismove.services.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("shops")
public class ShopController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private VendorMapper vendorMapper;
    @Autowired
    private CouponMapper couponMapper;

    @GetMapping("{shopId}/articles")
    public List<ArticleDto> getArticles(@PathVariable("shopId") Long id) {
        Shop shop = shopService.findById(id).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        return vendorMapper.articleToDto(shop.getArticles());
    }

    @PostMapping("{shopId}/articles")
    public ArticleDto createArticle(@PathVariable("shopId") Long shopId, @RequestBody ArticleDto dto) {
        Shop shop = shopService.findById(shopId).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        return shopService.createArticle(shop,dto);
    }

    @PutMapping("{shopId}/articles/{id}")
    public ArticleDto updateArticle(@PathVariable("shopId") Long shopId, @PathVariable("id") Long id, @RequestBody ArticleDto dto) {
        shopService.findById(shopId).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        Article article = shopService.findArticleById(id).orElseThrow(() -> new LismoveException("Article not found", HttpStatus.NOT_FOUND));
        return shopService.updateArticle(article,dto);
    }

    @DeleteMapping("{shopId}/articles/{id}")
    public ArticleDto deleteArticle(@PathVariable("shopId") Long shopId, @PathVariable("id") Long id) {
        shopService.findById(shopId).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        Article article = shopService.findArticleById(id).orElseThrow(() -> new LismoveException("Article not found", HttpStatus.NOT_FOUND));
        return shopService.deleteArticle(article);
    }

    @ApiOperation(value = "consume", notes = "Consuma il codice del coupon se il vendor del coupon coincide con il vendor proprietario dello shop che richiede di riscattarlo.")
    @PutMapping("{sid}/coupons/{code}")
    public CouponDto redeemCoupon(@PathVariable("sid") Long sid, @PathVariable String code){
        Shop shop = shopService.findById(sid).orElseThrow(() -> new LismoveException("Shop not found", HttpStatus.NOT_FOUND));
        Coupon coupon = couponService.findByCode(code).orElseThrow(() -> new LismoveException("Coupon not found", HttpStatus.NOT_FOUND));
        return couponMapper.couponToDto(couponService.redeem(coupon, shop));
    }
}
