package net.nextome.lismove.repositories;

import net.nextome.lismove.models.User;
import net.nextome.lismove.models.Vendor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VendorRepository extends CrudRepository<Vendor, Long> {
    Optional<Vendor> findByUserUid(String uid);
}
