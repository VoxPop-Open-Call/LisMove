package net.nextome.lismove.repositories;

import net.nextome.lismove.models.Article;
import org.springframework.data.repository.CrudRepository;

public interface ArticleRepository extends CrudRepository<Article, Long> {
}
