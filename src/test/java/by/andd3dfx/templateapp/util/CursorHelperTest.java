package by.andd3dfx.templateapp.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.dto.Cursor;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CursorHelperTest {

    private CursorHelper helper = new CursorHelper();

    @Test
    void decodeForNull() {
        Cursor cursor = helper.decode(null);

        assertNull(cursor);
    }

    @Test
    void encodeForNull() {
        String cursor = helper.encode(null);

        assertNull(cursor);
    }

    @Test
    void encodeNDecode() {
        Cursor cursor = buildCursor(123L);
        String encodedString = helper.encode(cursor);
        Cursor decodedCursor = helper.decode(encodedString);

        assertTrue(cursor.equals(decodedCursor));
    }

    @Test
    void encodeLengthRestriction() {
        final Cursor cursor = buildCursor(123L);

        String encodedString = helper.encode(cursor);

        assertThat(encodedString.length(), lessThan(105));
    }

    @Test
    void buildSearchCriteriaForForwardCursorWhenSortEncodedInCursor() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(true, id, "title", "Some value");

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, pageSize, null);

        assertThat(criteria.isForward(), is(true));
        assertThat(criteria.getId(), is(id));
        assertThat(criteria.getPageSize(), is(pageSize));
        assertThat(criteria.getSort(), is(cursor.getSortFieldName()));
        assertThat(criteria.getSortFieldValue(), is(cursor.getSortFieldValue()));
    }

    @Test
    void buildSearchCriteriaForForwardCursorWhenSortPassedAsParam() {
        final int pageSize = 35;
        final String sortFieldName = "title";

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(null, pageSize, sortFieldName);

        assertThat(criteria.isForward(), is(true));
        assertThat(criteria.getPageSize(), is(pageSize));
        assertThat(criteria.getSort(), is(sortFieldName));
        assertThat(criteria.getSortFieldValue(), nullValue());
    }

    @Test
    void buildSearchCriteriaForBackwardCursor() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(false, id, "title", "Some value");

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, pageSize, null);

        assertThat(criteria.isForward(), is(false));
        assertThat(criteria.getId(), is(id));
        assertThat(criteria.getPageSize(), is(pageSize));
        assertThat(criteria.getSort(), is(cursor.getSortFieldName()));
        assertThat(criteria.getSortFieldValue(), is(cursor.getSortFieldValue()));
    }

    @Test
    void buildSearchCriteriaWhenSortPopulatedAtTwoPlaces() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(true, id, "title", "Some value");

        try {
            helper.buildSearchCriteria(cursor, pageSize, "title");
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("Sort field name should be set in param OR inside the cursor"));
        }
    }

    @Test
    void buildSearchCriteriaWhenSortNamePopulatedButValueNot() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(true, id, "title", null);

        try {
            helper.buildSearchCriteria(cursor, pageSize, null);
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("Sort field name & value should be populated inside the cursor at the same time"));
        }
    }

    @Test
    void buildPrevLink() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String prevLink = helper.buildPrevLink(articles, null, null);

        assertThat(new CursorHelper().decode(prevLink).getId(), is(123L));
    }

    @Test
    void buildPrevLinkWhenExplicitSortPresents() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String prevLink = helper.buildPrevLink(articles, "title", null);

        assertThat(prevLink, nullValue());
    }

    @Test
    void buildPrevLinkForTitle() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String prevLink = helper.buildPrevLink(articles, null, "title");

        assertThat(new CursorHelper().decode(prevLink).getId(), is(123L));
    }

    @Test
    void buildNextLink() {
        Integer pageSize = 2;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, pageSize, null);

        assertThat(new CursorHelper().decode(nextLink).getId(), is(125L));
    }

    @Test
    void buildNextLinkForTitle() {
        Integer pageSize = 2;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, pageSize, "title");

        assertThat(new CursorHelper().decode(nextLink).getId(), is(125L));
    }

    @Test
    void buildNextLinkWhenNoRecordsInResult() {
        Integer pageSize = 50;
        List<ArticleDto> articles = Arrays.asList();

        String nextLink = helper.buildNextLink(articles, pageSize, null);

        assertThat(nextLink, nullValue());
    }

    @Test
    void buildNextLinkWhenRecordsAmountLessThanPageSize() {
        Integer pageSize = 50;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, pageSize, null);

        assertThat(nextLink, nullValue());
    }

    private ArticleDto buildArticle(long id) {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setId(id);
        articleDto.setTitle("Some tittle value");
        articleDto.setSummary("Some summary value");
        articleDto.setText("Some text");
        articleDto.setAuthor("John Deer");
        articleDto.setDateCreated(LocalDateTime.of(1980, 9, 21, 0, 0));
        articleDto.setDateUpdated(LocalDateTime.of(2011, 3, 5, 0, 0));
        return articleDto;
    }

    private Cursor buildCursor(Long id) {
        Cursor cursor = new Cursor();
        cursor.setId(id);
        cursor.setForward(true);
        cursor.setSortFieldName("title");
        cursor.setSortFieldValue("Слова подвижнические");
        return cursor;
    }
}
