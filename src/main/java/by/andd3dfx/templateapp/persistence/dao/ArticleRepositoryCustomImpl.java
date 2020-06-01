package by.andd3dfx.templateapp.persistence.dao;

import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.persistence.entities.Article;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    @Autowired
    private EntityManager em;

    @Override
    public List<Article> findByCriteria(ArticleSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Article> cq = cb.createQuery(Article.class);
        Root<Article> plan = cq.from(Article.class);

        final List<Predicate> predicates = buildPredicates(criteria, cb, plan);
        cq.where(predicates.toArray(new Predicate[0]));

        if (criteria.isBackward()) {
            final List<Order> orderList = Arrays.asList(cb.desc(plan.get("id")));
            cq.orderBy(orderList);
        }

        List<Article> result = em.createQuery(cq)
            .setMaxResults(criteria.getPageSize())
            .getResultList();

        if (criteria.isBackward()) {
            Collections.reverse(result);
        }
        return result;
    }

    private List<Predicate> buildPredicates(ArticleSearchCriteria criteria, CriteriaBuilder cb, Root plan) {
        List<Predicate> predicates = new ArrayList<>();
        if (criteria.getId() != null) {
            if (criteria.isForward()) {
                predicates.add(cb.greaterThan(plan.get("id"), criteria.getId()));
            } else {
                predicates.add(cb.lessThan(plan.get("id"), criteria.getId()));
            }
        }
        return predicates;
    }
}
