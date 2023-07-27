package net.nextome.lismove.services;

import com.google.maps.errors.ApiException;
import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.models.enums.AwardCustomIssuer;
import net.nextome.lismove.models.enums.AwardType;
import net.nextome.lismove.repositories.ArticleRepository;
import net.nextome.lismove.repositories.CategoryRepository;
import net.nextome.lismove.repositories.ShopRepository;
import net.nextome.lismove.repositories.VendorRepository;
import net.nextome.lismove.rest.dto.ArticleDto;
import net.nextome.lismove.rest.dto.ShopDto;
import net.nextome.lismove.rest.mappers.VendorMapper;
import net.nextome.lismove.services.utils.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShopService extends UtilitiesService {
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private VendorMapper vendorMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private AwardService awardService;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private GoogleMapsService gmapsService;
    @Autowired
    private ArticleRepository articleRepository;

    public ShopDto create(Vendor vendor, ShopDto dto) throws IOException, InterruptedException, ApiException {
        Shop shop = vendorMapper.dtoToShop(dto);
        shop.setVendor(vendor);
        if (shop.getAddress() != null) {
            Address address = gmapsService.generateAddress(shop.getAddress());
            mapAddressToShop(shop, address, shop.getLatitude() == null || shop.getLongitude() == null);
        }

        shop = shopRepository.save(shop);
        return vendorMapper.shopToDto(shop);
    }

    public ShopDto update(Shop dbShop, ShopDto dto) throws IOException, InterruptedException, ApiException {
        Shop newShop = vendorMapper.dtoToShop(dto);
        notNullBeanCopy(newShop, dbShop);

        if (newShop.getAddress() != null) {
            Address address = gmapsService.generateAddress(newShop.getAddress());
            mapAddressToShop(dbShop, address, newShop.getLatitude() == null || newShop.getLongitude() == null);
        }

        dbShop = shopRepository.save(dbShop);
        return vendorMapper.shopToDto(dbShop);
    }

    /**
     * inserisce in shop i dati dell'address generati da google maps
     *
     * @param shop    se lo shop ha già latitudine e longitudine settati allora non sovrascrivo
     * @param address oggetto con i dati dell' address
     */
    private void mapAddressToShop(Shop shop, Address address, boolean mapLatLng) {
        //update shop address
        shop.setAddress(address.getAddress());
        shop.setNumber(address.getNumber());
        shop.setCity(address.getCity());
        if (mapLatLng) {
            shop.setLatitude(address.getLatitude());
            shop.setLatitude(address.getLatitude());
        }
    }

    public ShopDto delete(Shop shop) {
        shopRepository.delete(shop);
        return vendorMapper.shopToDto(shop);
    }

    public Optional<Shop> findById(Long idShop) {
        return shopRepository.findById(idShop);
    }

    public ArticleDto createArticle(Shop shop, ArticleDto dto) {
        Article article = vendorMapper.dtoToArticle(dto);
        article.setShop(shop);
        article = articleRepository.save(article);
        return vendorMapper.articleToDto(article);
    }

    public ArticleDto updateArticle(Article dbArticle, ArticleDto dto) {
        Article newArticle = vendorMapper.dtoToArticle(dto);
        notNullBeanCopy(newArticle, dbArticle);
        dbArticle = articleRepository.save(dbArticle);
        return vendorMapper.articleToDto(dbArticle);
    }

    //TODO controllare che l'articolo non sia già stato acquistato
    public ArticleDto deleteArticle(Article article) {
        articleRepository.delete(article);
        return vendorMapper.articleToDto(article);
    }

    public AwardCustomUser buyArticle(User user, Article article) {
        if (article.getNumberArticles() != null && couponService.findAllByArticle(article).size() >= article.getNumberArticles()) {
            throw new LismoveException("Article out of stock", HttpStatus.BAD_REQUEST);
        }
        if (article.getPoints() != null && user.getPoints().compareTo(article.getPoints()) < 0) {
            throw new LismoveException("Not enough points", HttpStatus.BAD_REQUEST);
        }
        return awardService.generateAndAssign(
                AwardType.SHOP,
                article.getPoints(),
                AwardCustomIssuer.CREATED_BY_USER,
                article.getShop().getVendor().getOrganization(),
                1,
                "Acquisto di \"" + article.getTitle() + "\"",
                "Premio generato automaticamente in seguito a un acquisto in-app",
                null,
                user,
                article
        );
    }

    public Optional<Article> findArticleById(Long id) {
        return articleRepository.findById(id);
    }

    /**
     * defines if exist at least an article in shop list
     */
    public Boolean existsArticles(List<Shop> shops) {
        for (Shop shop : shops) {
            if ((long) shop.getArticles().size() > 0) {
                return true;
            }
        }
        return false;
    }
}
