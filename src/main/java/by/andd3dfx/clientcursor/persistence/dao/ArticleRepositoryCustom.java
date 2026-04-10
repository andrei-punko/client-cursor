package by.andd3dfx.clientcursor.persistence.dao;

import by.andd3dfx.clientcursor.dto.ArticleSearchCriteria;
import by.andd3dfx.clientcursor.persistence.entities.Article;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepositoryCustom {

    List<Article> findByCriteria(ArticleSearchCriteria criteria);
}
