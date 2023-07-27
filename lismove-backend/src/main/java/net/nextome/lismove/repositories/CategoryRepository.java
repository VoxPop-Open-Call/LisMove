package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {
}
