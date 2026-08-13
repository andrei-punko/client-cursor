package by.andd3dfx.clientcursor.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import by.andd3dfx.clientcursor.exceptions.BadRequestException;
import by.andd3dfx.clientcursor.dto.ArticleDto;
import by.andd3dfx.clientcursor.dto.ArticleSearchCriteria;
import by.andd3dfx.clientcursor.dto.Cursor;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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
    void decodeForWrongString() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            helper.decode("asdf jkl;");
        });
    }

    @Test
    void encodeUsesShortJsonKeysOnly() {
        Cursor cursor = new Cursor(true, 5L, null, null, "ASC");

        String json = new String(Base64.getDecoder().decode(helper.encode(cursor)), StandardCharsets.UTF_8);

        assertThat(json.contains("\"f\":true"), is(true));
        assertThat(json.contains("forward"), is(false));
        assertThat(json.contains("\"i\":5"), is(true));
    }

    @Test
    void encodeLengthRestriction() {
        final Cursor cursor = buildCursor(123L);

        String encodedString = helper.encode(cursor);

        assertThat(encodedString.length(), lessThan(200));
    }

    @Test
    void buildSearchCriteriaForForwardCursorWhenSortEncodedInCursor() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(true, id, "title", "Some value", "ASC");

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, pageSize, null, "ASC");

        assertThat(criteria.isForward(), is(true));
        assertThat(criteria.getId(), is(id));
        assertThat(criteria.getPageSize(), is(pageSize));
        assertThat(criteria.getSortFieldName(), is(cursor.getSortFieldName()));
        assertThat(criteria.getSortFieldValue(), is(cursor.getSortFieldValue()));
    }

    @Test
    void buildSearchCriteriaForForwardCursorWhenSortPassedAsParam() {
        final int pageSize = 35;
        final String sortFieldName = "title";

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(null, pageSize, sortFieldName, "ASC");

        assertThat(criteria.isForward(), is(true));
        assertThat(criteria.getPageSize(), is(pageSize));
        assertThat(criteria.getSortFieldName(), is(sortFieldName));
        assertThat(criteria.getSortFieldValue(), nullValue());
    }

    @Test
    void buildSearchCriteriaForBackwardCursor() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(false, id, "title","Some value", "ASC");

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, pageSize, null, "ASC");

        assertThat(criteria.isForward(), is(false));
        assertThat(criteria.getId(), is(id));
        assertThat(criteria.getPageSize(), is(pageSize));
        assertThat(criteria.getSortFieldName(), is(cursor.getSortFieldName()));
        assertThat(criteria.getSortFieldValue(), is(cursor.getSortFieldValue()));
    }

    @Test
    void buildSearchCriteriaWhenSortPopulatedAtTwoPlaces() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(true, id, "title", "Some value", "ASC");

        try {
            helper.buildSearchCriteria(cursor, pageSize,  "title", "ASC");
            fail("Exception should be thrown");
        } catch (BadRequestException iae) {
            assertThat(iae.getMessage(), is(
                "Do not pass query parameter 'sort_by' together with 'cursor'; sort is already encoded in the cursor"
            ));
        }
    }

    @Test
    void buildSearchCriteriaWhenSortNamePopulatedButValueNot() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = new Cursor(true, id, "title", null, "ASC");

        try {
            helper.buildSearchCriteria(cursor, pageSize,  null, "ASC");
            fail("Exception should be thrown");
        } catch (BadRequestException iae) {
            assertThat(iae.getMessage(), is("Sort field name & value should be populated inside the cursor at the same time"));
        }
    }

    @Test
    void buildSearchCriteriaWhenUnsupportedSortPassedAsParam() {
        Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(null, 35, "unknown", "ASC")
        );
    }

    @Test
    void buildSearchCriteriaWhenUnsupportedSortEncodedInCursor() {
        Cursor cursor = new Cursor(true, 123L, "text", "Some value", "ASC");

        Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(cursor, 35, null, "ASC")
        );
    }

    @Test
    void buildSearchCriteriaForAllowedSortFields() {
        for (String sortFieldName : new String[]{"title", "author", "summary"}) {
            ArticleSearchCriteria criteria = helper.buildSearchCriteria(null, 35, sortFieldName, "ASC");
            assertThat(criteria.getSortFieldName(), is(sortFieldName));
        }
    }

    @Test
    void buildSearchCriteriaWhenPageSizeIsNull() {
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(null, null, "title", "ASC")
        );
        assertThat(ex.getMessage(), is("Page size must be a positive integer"));
    }

    @Test
    void buildSearchCriteriaWhenPageSizeIsNotPositive() {
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(null, 0, "title", "ASC")
        );
        assertThat(ex.getMessage(), is("Page size must be a positive integer"));
    }

    @Test
    void buildSearchCriteriaWhenPageSizeExceedsMax() {
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(null, CursorHelper.MAX_PAGE_SIZE + 1, "title", "ASC")
        );
        assertThat(ex.getMessage(), is("Page size must not be greater than " + CursorHelper.MAX_PAGE_SIZE));
    }

    @Test
    void buildSearchCriteriaWhenUnsupportedOrderPassedAsParam() {
        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(null, 35, "title", "UP")
        );
        assertThat(ex.getMessage(), is("Unsupported sort order: 'UP'. Allowed: ASC, DESC"));
    }

    @Test
    void buildSearchCriteriaWhenUnsupportedOrderEncodedInCursor() {
        Cursor cursor = new Cursor(true, 123L, "title", "Some value", "DOWN");

        BadRequestException ex = Assertions.assertThrows(BadRequestException.class, () ->
            helper.buildSearchCriteria(cursor, 35, null, "ASC")
        );
        assertThat(ex.getMessage(), is("Unsupported sort order: 'DOWN'. Allowed: ASC, DESC"));
    }

    @Test
    void buildSearchCriteriaWhenOrderIsNullDefaultsToAsc() {
        ArticleSearchCriteria criteria = helper.buildSearchCriteria(null, 35, "title", null);

        assertThat(criteria.getSortOrder(), is(ArticleSearchCriteria.SortOrder.ASC));
    }

    @Test
    void buildPrevLink() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));
        String prevLink = helper.buildPrevLink(articles, true, null, "ASC");

        assertThat(new CursorHelper().decode(prevLink).getId(), is(123L));
    }

    @Test
    void buildPrevLinkWhenHasPrevIsFalse() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String prevLink = helper.buildPrevLink(articles, false, "title", "ASC");

        assertThat(prevLink, nullValue());
    }

    @Test
    void buildPrevLinkForTitle() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));
        String prevLink = helper.buildPrevLink(articles, true, "title", "ASC");

        Cursor decoded = helper.decode(prevLink);
        assertThat(decoded.getId(), is(123L));
        assertThat(decoded.getSortOrder(), is("ASC"));
    }

    @Test
    void buildPrevLinkPreservesDescSortOrder() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));
        String prevLink = helper.buildPrevLink(articles, true, "title", "DESC");

        Cursor decoded = helper.decode(prevLink);
        assertThat(decoded.getId(), is(123L));
        assertThat(decoded.getSortFieldName(), is("title"));
        assertThat(decoded.getSortOrder(), is("DESC"));
    }

    @Test
    void buildNextLink() {
        Integer pageSize = 2;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, true, null, "ASC");

        assertThat(new CursorHelper().decode(nextLink).getId(), is(125L));
    }

    @Test
    void buildNextLinkForTitle() {
        Integer pageSize = 2;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, true, "title", "ASC");

        Cursor decoded = new CursorHelper().decode(nextLink);
        assertThat(decoded.getId(), is(125L));
        assertThat(decoded.getSortFieldName(), is("title"));
        assertThat(decoded.getSortFieldValue(), is("Some tittle value"));
        assertThat(decoded.getSortOrder(), is("ASC"));
    }

    @Test
    void buildNextLinkPreservesDescSortOrder() {
        Integer pageSize = 2;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, true, "title", "DESC");

        Cursor decoded = helper.decode(nextLink);
        assertThat(decoded.getId(), is(125L));
        assertThat(decoded.getSortFieldName(), is("title"));
        assertThat(decoded.getSortFieldValue(), is("Some tittle value"));
        assertThat(decoded.getSortOrder(), is("DESC"));
    }

    @Test
    void buildNextLinkForAuthor() {
        Integer pageSize = 2;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, true, "author", "ASC");

        Cursor decoded = helper.decode(nextLink);
        assertThat(decoded.getId(), is(125L));
        assertThat(decoded.getSortFieldName(), is("author"));
        assertThat(decoded.getSortFieldValue(), is("John Deer"));
    }

    @Test
    void buildPrevLinkForSummary() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));
        String prevLink = helper.buildPrevLink(articles, true, "summary", "ASC");

        Cursor decoded = helper.decode(prevLink);
        assertThat(decoded.getId(), is(123L));
        assertThat(decoded.getSortFieldName(), is("summary"));
        assertThat(decoded.getSortFieldValue(), is("Some summary value"));
    }

    @Test
    void descSortOrderSurvivesNextCursorRoundTrip() {
        ArticleSearchCriteria firstPage = helper.buildSearchCriteria(null, 2, "title", "DESC");
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, true, firstPage.getSortFieldName(),
            firstPage.getSortOrder().name());
        Cursor nextCursor = helper.decode(nextLink);
        ArticleSearchCriteria secondPage = helper.buildSearchCriteria(nextCursor, 2, null, null);

        assertThat(secondPage.getSortOrder(), is(ArticleSearchCriteria.SortOrder.DESC));
        assertThat(secondPage.getSortFieldName(), is("title"));
        assertThat(secondPage.isForward(), is(true));
    }

    @Test
    void buildSearchCriteriaWhenDescPassedAsParam() {
        ArticleSearchCriteria criteria = helper.buildSearchCriteria(null, 35, "title", "DESC");

        assertThat(criteria.getSortOrder(), is(ArticleSearchCriteria.SortOrder.DESC));
        assertThat(criteria.getSortFieldName(), is("title"));
    }

    @Test
    void buildSearchCriteriaWhenDescEncodedInCursor() {
        Cursor cursor = new Cursor(true, 123L, "title", "Some value", "DESC");

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, 35, null, "ASC");

        assertThat(criteria.getSortOrder(), is(ArticleSearchCriteria.SortOrder.DESC));
    }

    @Test
    void buildNextLinkWhenNoRecordsInResult() {
        Integer pageSize = 50;
        List<ArticleDto> articles = Arrays.asList();

        String nextLink = helper.buildNextLink(articles, true, null, "ASC");

        assertThat(nextLink, nullValue());
    }

    @Test
    void buildNextLinkWhenRecordsAmountLessThanPageSize() {
        Integer pageSize = 50;
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles, false, null, "DESC");

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
