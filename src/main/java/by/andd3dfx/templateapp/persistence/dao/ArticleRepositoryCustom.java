package by.andd3dfx.templateapp.persistence.dao;

import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.persistence.entities.Article;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepositoryCustom {

    List<Article> findByCriteria(ArticleSearchCriteria criteria);
}
