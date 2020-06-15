package by.andd3dfx.templateapp.persistence.dao;

import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria.SortOrder;
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
        Check this page for details: https://itnan.ru/post.php?c=1&p=419083

        * Query for forward cursor:
        SELECT * FROM items WHERE ...            -- apply search params
        AND ((fieldName = :nextCursor.fieldName AND sequentialId > :nextCursor.sequentialId) OR
        fieldName > :nextCursor.fieldName)
        ORDER BY :sortingFieldName, :sequentialId
        LIMIT :count

        * Query for backward cursor:
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

        String sortFieldName = criteria.getSortFieldName();
        if (sortFieldName != null) {
            String sortFieldValue = criteria.getSortFieldValue();

            if (sortFieldValue != null) {
                Predicate equalFieldPredicate = cb.equal(plan.get(sortFieldName), sortFieldValue);
                Expression<Boolean> predicate1 = (criteria.getId() != null) ?
                    cb.and(equalFieldPredicate, buildIdPredicate(criteria, cb, plan)) :
                    equalFieldPredicate;

                Expression<Boolean> predicate2 = buildSortFieldPredicate(criteria, cb, plan, sortFieldName, sortFieldValue, criteria.getSortOrder());
                cq.where(cb.or(predicate1, predicate2));
            } else if (criteria.getId() != null) {
                cq.where(buildIdPredicate(criteria, cb, plan));
            }
        } else if (criteria.getId() != null) {
            cq.where(buildIdPredicate(criteria, cb, plan));
        }

        final List<Order> orders = new ArrayList<>();
        if (sortFieldName != null) {
            if (criteria.getSortOrder() == SortOrder.ASC) {
                final Order orderBySortingField = criteria.isForward() ?
                    cb.asc(plan.get(sortFieldName)) :
                    cb.desc(plan.get(sortFieldName));
                orders.add(orderBySortingField);
            } else {
                final Order orderBySortingField = criteria.isForward() ?
                    cb.desc(plan.get(sortFieldName)) :
                    cb.asc(plan.get(sortFieldName));
                orders.add(orderBySortingField);
            }
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
        String sortFieldValue, SortOrder sortOrder) {
        if (sortOrder == SortOrder.ASC) {
            return criteria.isForward() ?
                cb.greaterThan(plan.get(sortFieldName), sortFieldValue) :
                cb.lessThan(plan.get(sortFieldName), sortFieldValue);
        } else {
            return criteria.isForward() ?
                cb.lessThan(plan.get(sortFieldName), sortFieldValue) :
                cb.greaterThan(plan.get(sortFieldName), sortFieldValue);
        }
    }

    private Predicate buildIdPredicate(ArticleSearchCriteria criteria, CriteriaBuilder cb, Root<Article> plan) {
        return criteria.isForward() ?
            cb.greaterThan(plan.get("id"), criteria.getId()) :
            cb.lessThan(plan.get("id"), criteria.getId());
    }
}
