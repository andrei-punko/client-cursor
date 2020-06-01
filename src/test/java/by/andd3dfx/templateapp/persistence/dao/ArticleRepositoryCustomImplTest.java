package by.andd3dfx.templateapp.persistence.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.persistence.entities.Article;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@ExtendWith(MockitoExtension.class)
@DataJpaTest
class ArticleRepositoryCustomImplTest {

    @Autowired
    private ArticleRepository repository;

    private Article entity;
    private Article entity2;
    private Article entity3;

    @BeforeEach
    public void setup() {
        repository.deleteAll();
        entity = buildArticle("Ivan M.", "HD", LocalDateTime.parse("2010-12-03T10:15:30"));
        entity2 = buildArticle("Vasily", "HD", LocalDateTime.parse("2011-12-03T10:15:30"));
        entity3 = buildArticle("Ivan A.", "4K", LocalDateTime.parse("2012-12-03T10:15:30"));
        repository.saveAll(Arrays.asList(entity, entity2, entity3));
    }

    @AfterEach
    public void tearDown() {
        repository.deleteAll();
    }

    @Test
    public void findByCriteriaWithoutParams() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(3));
        assertThat("Not found some entity", result.containsAll(Arrays.asList(entity, entity2, entity3)), is(true));
    }

    @Test
    public void findByCriteriaWithSort() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setSort("title");

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(3));
        assertThat("Wrong records[0] value", result.get(0), is(entity3));
        assertThat("Wrong records[1] value", result.get(1), is(entity));
        assertThat("Wrong records[2] value", result.get(2), is(entity2));
    }

    @Test
    public void findByCriteriaWithSortNPageSize() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setSort("title");
        criteria.setPageSize(2);

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(2));
        assertThat("Wrong records[0] value", result.get(0), is(entity3));
        assertThat("Wrong records[1] value", result.get(1), is(entity));
    }

    @Test
    public void findByCriteriaWithSortNIdNPageSize() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setSort("title");
        criteria.setId(entity3.getId());
        criteria.setSortFieldValue(entity3.getTitle());
        criteria.setPageSize(1);

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(1));
        assertThat("Wrong records[0] value", result.get(0), is(entity));
    }

    @Test
    public void findByCriteriaWithSortNId() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setSort("title");
        criteria.setId(entity3.getId());
        criteria.setSortFieldValue(entity3.getTitle());

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(2));
        assertThat("Wrong records[0] value", result.get(0), is(entity));
        assertThat("Wrong records[1] value", result.get(1), is(entity2));
    }

    @Test
    public void findByCriteriaWithId() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setForward(true);
        criteria.setId(entity.getId());

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(2));
        assertThat("Not found some entity", result.containsAll(Arrays.asList(entity2, entity3)), is(true));
    }

    @Test
    public void findByCriteriaWithIdNBackwardCursor() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setForward(false);
        criteria.setId(entity2.getId());

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(1));
        assertThat("Not found some entity", result.containsAll(Arrays.asList(entity)), is(true));
    }

    @Test
    public void findByCriteriaWithPageSize() {
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setPageSize(2);

        List<Article> result = repository.findByCriteria(criteria);

        assertThat("Wrong records amount", result.size(), is(2));
        assertThat("Not found some entity", result.containsAll(Arrays.asList(entity, entity2)), is(true));
    }

    private Article buildArticle(String title, String summary, LocalDateTime timestamp) {
        Article article = new Article();
        article.setTitle(title);
        article.setSummary(summary);
        article.setText("any text");
        article.setTimestamp(timestamp);
        article.setAuthor("Some Author");
        return article;
    }
}
