package by.andd3dfx.clientcursor.persistence.dao;

import by.andd3dfx.clientcursor.persistence.entities.Article;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends CrudRepository<Article, Long>, ArticleRepositoryCustom {

}
