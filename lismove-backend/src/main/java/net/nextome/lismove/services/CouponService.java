package net.nextome.lismove.services;

import net.nextome.lismove.exceptions.LismoveException;
import net.nextome.lismove.models.*;
import net.nextome.lismove.repositories.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public Coupon generate(AwardAchievementUser award) {
        return generate(award.getAwardAchievement(), award.getAchievementUser().getUser(), award.getAchievementUser().getAchievement().getOrganization(), null);
    }

    public Coupon generate(AwardCustomUser award) {
        return generate(award.getAwardCustom(), award.getUser(), award.getAwardCustom().getOrganization(), null);
    }

    public Coupon generate(AwardCustomUser award, Article article) {
        return generate(award.getAwardCustom(), award.getUser(), award.getAwardCustom().getOrganization(), article);
    }

    public Coupon generate(AwardPositionUser award) {
        return generate(award.getAwardPosition(), award.getUser(), award.getAwardPosition().getOrganization(), null);
    }

    public Coupon generate(AwardRanking award) {
        return generate(award, award.getUser(), award.getRanking().getOrganization(), null);
    }

    private Coupon generate(Award award, User user, Organization org, Article article) {
        Coupon coupon = new Coupon();
        coupon.setAwardType(award.getType());
        coupon.setName(award.getName());
        coupon.setDescription(award.getDescription());
        coupon.setValue(award.getValue());
        coupon.setUser(user);
        coupon.setOrganization(org);
        coupon.setArticle(article);
        if (article != null) {
            coupon.setShop(article.getShop());
        }
        coupon.setEmissionDate(LocalDateTime.now());
        String ts = UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
        coupon.setCode(org != null ? org.getCode() : "NT" + ts.substring(ts.length() - 6));
        return couponRepository.save(coupon);
    }

    /**
     * setta la data di rimborso della lista di coupons e restituisce un map chiave valore con codice -> "refund"/messaggio di errore
     *
     * @param coupons
     * @return
     */
    public Map<String, String> refund(Set<Coupon> coupons) {
        return refund(coupons, false);
    }

    /**
     * setta la data di rimborso della lista di coupons a null e restituisce un map chiave valore con codice -> "refund"/messaggio di errore
     *
     * @param coupons
     * @return
     */
    public Map<String, String> reverseRefund(Set<Coupon> coupons) {
        return refund(coupons, true);
    }

    private Map<String, String> refund(Set<Coupon> coupons, boolean isReverse) {
        Map<String, String> results = new HashMap<>();
        for (Coupon coupon : coupons) {
            try {
                if (isReverse) {
                    reverseRefund(coupon);
                } else {
                    refund(coupon);
                }
                results.put(coupon.getCode(), "refund");
            } catch (Exception e) {
                results.put(coupon.getCode(), e.getMessage());
            }
        }
        return results;
    }

    public Coupon reverseRefund(Coupon coupon) {
//        checkCouponValidity(coupon);
        if(coupon.getRedemptionDate() == null){
            throw new LismoveException("Coupon non riscattato", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getRefundDate() == null) {
            throw new LismoveException("Coupon non ancora rimborsato", HttpStatus.BAD_REQUEST);
        }
        coupon.setRefundDate(null);
        return couponRepository.save(coupon);
    }

    // Flow 1
    public Coupon refund(Coupon coupon) {
//        checkCouponValidity(coupon);
        if(coupon.getRedemptionDate() == null){
            throw new LismoveException("Coupon non riscattato", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getRefundDate() != null) {
            throw new LismoveException("Coupon già rimborsato", HttpStatus.BAD_REQUEST);
        }
        coupon.setRefundDate(LocalDateTime.now());
        return couponRepository.save(coupon);
    }

    // Flows 2, 3 and 4
    public Coupon redeem(Coupon coupon, Shop shop) {
        checkCouponValidity(coupon);
        if (coupon.getRedemptionDate() != null) {
            throw new LismoveException("Coupon già riscattato", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getOrganization() != null && shop.getVendor().getOrganization() != null && !coupon.getOrganization().getId().equals(shop.getVendor().getOrganization().getId())) {
            throw new LismoveException("Coupon non riscattabile da questo esercente", HttpStatus.FORBIDDEN);
        }
        if (coupon.getArticle() != null && !coupon.getArticle().getShop().equals(shop)) {
            throw new LismoveException("Coupon non riscattabile da questo esercente", HttpStatus.FORBIDDEN);
        }
        coupon.setShop(shop);
        coupon.setRedemptionDate(LocalDateTime.now());
        return couponRepository.save(coupon);
    }

    private void checkCouponValidity(Coupon coupon) {
        if (coupon.getExpireDate() != null && coupon.getExpireDate().compareTo(LocalDate.now()) < 0) {
            throw new LismoveException("Coupon scaduto o già riscattato", HttpStatus.BAD_REQUEST);
        }
        if (coupon.getEmissionDate() == null || coupon.getEmissionDate().compareTo(LocalDateTime.now()) > 0) {
            throw new LismoveException("Coupon scaduto o già riscattato", HttpStatus.BAD_REQUEST);
        }
    }

    public List<Coupon> findAllByUser(User user) {
        return couponRepository.findAllByUser(user);
    }

    public Optional<Coupon> findByCode(String code) {
        return couponRepository.findByCode(code);
    }

    public List<Coupon> findAllByArticle(Article article) {
        return couponRepository.findAllByArticle(article);
    }

    public List<Coupon> findByOrganization(Organization organization) {
        return couponRepository.findAllByOrganization(organization);
    }

    public List<Coupon> findAllNational() {
        return couponRepository.findAllByOrganizationIsNull();
    }
}
