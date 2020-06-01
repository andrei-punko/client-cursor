package by.andd3dfx.templateapp.persistence.dao;

import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.persistence.entities.Article;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    @Autowired
    private EntityManager em;

    /*
        SELECT * FROM items WHERE ...            -- apply search params
        AND ((fieldName = :nextCursor.fieldName AND sequentialId > :nextCursor.sequentialId) OR
        fieldName > :nextCursor.fieldName)
        ORDER BY :sortingFieldName, :sequentialId
        LIMIT :count

        SELECT * from items WHERE ...            -- apply search params
        AND ((fieldName = :prevCursor.fieldName AND seq_id < :prevCursor.sequentialId) OR
        fieldName < :prevCursor.fieldName)
        ORDER BY :sortingFieldName DESC, :sequentialId DESC
        LIMIT :count
    */
    @Override
    public List<Article> findByCriteria(ArticleSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Article> cq = cb.createQuery(Article.class);
        Root<Article> plan = cq.from(Article.class);

        String sortFieldName = criteria.getSort();
        if (sortFieldName != null) {
            String sortFieldValue = criteria.getSortFieldValue();
            if (sortFieldValue == null) {
                throw new IllegalArgumentException("Both 'sort' and 'sortFieldValue' fields should be populated!");
            }

            if (criteria.getId() != null) {
                final Expression<Boolean> predicate1 = cb.and(
                    cb.equal(plan.get(sortFieldName), sortFieldValue),
                    buildIdPredicate(criteria, cb, plan)
                );
                final Expression<Boolean> predicate2 = buildSortFieldPredicate(criteria, cb, plan, sortFieldName, sortFieldValue);
                cq.where(cb.or(predicate1, predicate2));
            } else {
                final Expression<Boolean> predicate1 = cb.equal(plan.get(sortFieldName), sortFieldValue);
                final Expression<Boolean> predicate2 = buildSortFieldPredicate(criteria, cb, plan, sortFieldName, sortFieldValue);
                cq.where(cb.or(predicate1, predicate2));
            }
        } else if (criteria.getId() != null) {
            cq.where(buildIdPredicate(criteria, cb, plan));
        }

        final List<Order> orders = new ArrayList<>();
        if (sortFieldName != null) {
            final Order orderBySortingField = criteria.isForward() ?
                cb.asc(plan.get(sortFieldName)) :
                cb.desc(plan.get(sortFieldName));
            orders.add(orderBySortingField);
        }

        final Order orderById = criteria.isForward() ?
            cb.asc(plan.get("id")) :
            cb.desc(plan.get("id"));
        orders.add(orderById);
        cq.orderBy(orders);

        List<Article> result = em.createQuery(cq)
            .setMaxResults(criteria.getPageSize())
            .getResultList();

        if (criteria.isBackward()) {
            Collections.reverse(result);
        }
        return result;
    }

    private Predicate buildSortFieldPredicate(ArticleSearchCriteria criteria, CriteriaBuilder cb, Root<Article> plan, String sortFieldName,
        String sortFieldValue) {
        return criteria.isForward() ?
            cb.greaterThan(plan.get(sortFieldName), sortFieldValue) :
            cb.lessThan(plan.get(sortFieldName), sortFieldValue);
    }

    private Predicate buildIdPredicate(ArticleSearchCriteria criteria, CriteriaBuilder cb, Root<Article> plan) {
        return criteria.isForward() ?
            cb.greaterThan(plan.get("id"), criteria.getId()) :
            cb.lessThan(plan.get("id"), criteria.getId());
    }
}
