package by.andd3dfx.templateapp.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
    void buildSearchCriteriaForForwardCursor() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = buildCursor(id);

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, pageSize);

        assertThat(criteria.getIdFrom(), is(id));
        assertThat(criteria.getIdTo(), nullValue());
        assertThat(criteria.getPageSize(), is(pageSize));
    }

    @Test
    void buildSearchCriteriaForBackwardCursor() {
        final long id = 123L;
        final int pageSize = 35;
        Cursor cursor = buildCursor(id);
        cursor.setForward(false);

        ArticleSearchCriteria criteria = helper.buildSearchCriteria(cursor, pageSize);

        assertThat(criteria.getIdFrom(), nullValue());
        assertThat(criteria.getIdTo(), is(id));
        assertThat(criteria.getPageSize(), is(pageSize));
    }

    @Test
    void buildPrevLink() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String prevLink = helper.buildPrevLink(articles);

        assertThat(new CursorHelper().decode(prevLink).getId(), is(123L));
    }

    @Test
    void buildNextLink() {
        List<ArticleDto> articles = Arrays.asList(buildArticle(123L), buildArticle(125L));

        String nextLink = helper.buildNextLink(articles);

        assertThat(new CursorHelper().decode(nextLink).getId(), is(125L));
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
        return cursor;
    }
}
