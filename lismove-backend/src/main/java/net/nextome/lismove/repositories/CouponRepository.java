package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Article;
import net.nextome.lismove.models.Coupon;
import net.nextome.lismove.models.Organization;
import net.nextome.lismove.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends CrudRepository<Coupon, Long> {

    List<Coupon> findAllByUser(User user);

    Optional<Coupon> findByCode(String code);

    List<Coupon> findAllByOrganization(Organization organization);

    List<Coupon> findAllByOrganizationIsNull();

    List<Coupon> findAllByArticle(Article article);
}
