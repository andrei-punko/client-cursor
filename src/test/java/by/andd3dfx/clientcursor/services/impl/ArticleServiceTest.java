package by.andd3dfx.clientcursor.services.impl;

import by.andd3dfx.clientcursor.dto.*;
import by.andd3dfx.clientcursor.exceptions.ArticleNotFoundException;
import by.andd3dfx.clientcursor.mappers.ArticleMapper;
import by.andd3dfx.clientcursor.persistence.dao.ArticleRepository;
import by.andd3dfx.clientcursor.persistence.entities.Article;
import by.andd3dfx.clientcursor.util.CursorHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepositoryMock;

    @Mock
    private ArticleMapper articleMapperMock;

    @Mock
    private CursorHelper cursorHelperMock;

    @Mock
    private Clock clockMock;
    private Clock fixedClock;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    public void before() {
        fixedClock = Clock.fixed(Instant.parse("2014-12-22T10:15:30.00Z"), ZoneId.systemDefault());
        // Allow unnecessary stubbing:
        lenient().doReturn(fixedClock.instant()).when(clockMock).instant();
        lenient().doReturn(fixedClock.getZone()).when(clockMock).getZone();
    }

    @Test
    void create() {
        ArticleDto articleDto = new ArticleDto();
        Article article = new Article();
        Article updatedArticle = new Article();
        ArticleDto updatedArticleDto = new ArticleDto();

        Mockito.when(articleMapperMock.toArticle(articleDto)).thenReturn(article);
        Mockito.when(articleRepositoryMock.save(article)).thenReturn(updatedArticle);
        Mockito.when(articleMapperMock.toArticleDto(updatedArticle)).thenReturn(updatedArticleDto);

        ArticleDto result = articleService.create(articleDto);

        Mockito.verify(articleMapperMock).toArticle(articleDto);
        Mockito.verify(articleRepositoryMock).save(article);
        Mockito.verify(articleMapperMock).toArticleDto(updatedArticle);
        assertThat(result, is(updatedArticleDto));
    }

    @Test
    public void get() {
        final Long ARTICLE_ID = 123L;
        Article article = new Article();
        Optional<Article> optionalArticle = Optional.of(article);
        ArticleDto articleDto = new ArticleDto();
        Mockito.when(articleRepositoryMock.findById(ARTICLE_ID)).thenReturn(optionalArticle);
        Mockito.when(articleMapperMock.toArticleDto(article)).thenReturn(articleDto);

        ArticleDto result = articleService.get(ARTICLE_ID);

        Mockito.verify(articleRepositoryMock).findById(ARTICLE_ID);
        Mockito.verify(articleMapperMock).toArticleDto(article);
        assertThat(result, is(articleDto));
    }

    @Test
    public void getAbsentArticle() {
        final Long ARTICLE_ID = 123L;
        Optional<Article> optionalArticle = Optional.empty();
        Mockito.when(articleRepositoryMock.findById(ARTICLE_ID)).thenReturn(optionalArticle);

        try {
            articleService.get(ARTICLE_ID);

            fail("Exception should be thrown");
        } catch (ArticleNotFoundException ex) {
            Mockito.verify(articleRepositoryMock).findById(ARTICLE_ID);
        }
    }

    @Test
    void update() {
        final Long ARTICLE_ID = 123L;
        Article article = new Article();
        Article savedArticle = new Article();
        Optional<Article> optionalArticle = Optional.of(article);
        ArticleUpdateDto articleUpdateDto = new ArticleUpdateDto();
        ArticleDto updatedArticleDto = new ArticleDto();
        updatedArticleDto.setTitle("New title");

        Mockito.when(articleRepositoryMock.findById(ARTICLE_ID)).thenReturn(optionalArticle);
        Mockito.when(articleRepositoryMock.save(article)).thenReturn(savedArticle);
        Mockito.when(articleMapperMock.toArticleDto(savedArticle)).thenReturn(updatedArticleDto);

        ArticleDto result = articleService.update(ARTICLE_ID, articleUpdateDto);

        Mockito.verify(articleRepositoryMock).findById(ARTICLE_ID);
        Mockito.verify(articleMapperMock).toArticle(articleUpdateDto, article);
        Mockito.verify(articleRepositoryMock).save(article);
        Mockito.verify(articleMapperMock).toArticleDto(savedArticle);
        assertThat(result, is(updatedArticleDto));
    }

    @Test
    void updateAbsentArticle() {
        final Long ARTICLE_ID = 123L;
        Optional<Article> optionalArticle = Optional.empty();
        Mockito.when(articleRepositoryMock.findById(ARTICLE_ID)).thenReturn(optionalArticle);
        ArticleUpdateDto articleUpdateDto = new ArticleUpdateDto();

        try {
            articleService.update(ARTICLE_ID, articleUpdateDto);

            fail("Exception should be thrown");
        } catch (ArticleNotFoundException ex) {
            Mockito.verify(articleRepositoryMock).findById(ARTICLE_ID);
        }
    }

    @Test
    void delete() {
        final Long ARTICLE_ID = 123L;
        Mockito.when(articleRepositoryMock.existsById(ARTICLE_ID)).thenReturn(true);

        articleService.delete(ARTICLE_ID);

        Mockito.verify(articleRepositoryMock).existsById(ARTICLE_ID);
        Mockito.verify(articleRepositoryMock).deleteById(ARTICLE_ID);
    }

    @Test
    void deleteAbsentArticle() {
        final Long ARTICLE_ID = 1231L;
        Mockito.when(articleRepositoryMock.existsById(ARTICLE_ID)).thenReturn(false);

        try {
            articleService.delete(ARTICLE_ID);

            fail("Exception should be thrown");
        } catch (ArticleNotFoundException ex) {
            Mockito.verify(articleRepositoryMock).existsById(ARTICLE_ID);
            Mockito.verify(articleRepositoryMock, Mockito.never()).deleteById(ARTICLE_ID);
            assertThat("Wrong message", ex.getMessage(), is("Could not find an article by id=" + ARTICLE_ID));
        }
    }

    @Test
    public void getByCursor() {
        final String encodedCursor = "some-encoded-cursor";
        final int pageSize = 50;
        final String sortFieldName = "some-sort-field-name";
        final String sortOrder = "some-sort-order";
        final Cursor cursor = new Cursor();

        Mockito.when(cursorHelperMock.decode(encodedCursor)).thenReturn(cursor);
        final ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        Mockito.when(cursorHelperMock.buildSearchCriteria(cursor, pageSize, sortFieldName, sortOrder)).thenReturn(criteria);
        final List<Article> articles = Arrays.asList(new Article());
        Mockito.when(articleRepositoryMock.findByCriteria(criteria)).thenReturn(articles);
        final List<ArticleDto> articleDtos = Arrays.asList(new ArticleDto());
        Mockito.when(articleMapperMock.toArticleDtoList(articles)).thenReturn(articleDtos);
        final String prevLink = "some-prev-link";
        Mockito.when(cursorHelperMock.buildPrevLink(articleDtos, true, criteria.getSortFieldName(), "ASC")).thenReturn(prevLink);
        final String nextLink = "some-next-link";
        Mockito.when(cursorHelperMock.buildNextLink(articleDtos, false, criteria.getSortFieldName(), "ASC")).thenReturn(nextLink);

        CursorResponse<ArticleDto> response = articleService
                .getByCursor(encodedCursor, pageSize, sortFieldName, sortOrder);

        assertThat(response.getData(), is(articleDtos));
        assertThat(response.getPrev(), is(prevLink));
        assertThat(response.getNext(), is(nextLink));

        Mockito.verify(cursorHelperMock).decode(encodedCursor);
        Mockito.verify(cursorHelperMock).buildSearchCriteria(cursor, pageSize, sortFieldName, sortOrder);
        Mockito.verify(articleRepositoryMock).findByCriteria(criteria);
        Mockito.verify(articleMapperMock).toArticleDtoList(articles);
        Mockito.verify(cursorHelperMock).buildPrevLink(articleDtos, true, criteria.getSortFieldName(), "ASC");
        Mockito.verify(cursorHelperMock).buildNextLink(articleDtos, false, criteria.getSortFieldName(), "ASC");
    }

    @Test
    public void getByCursorBackwardToFirstPageHasNoPrev() {
        final String encodedCursor = "some-encoded-cursor";
        final int pageSize = 2;
        final Cursor cursor = new Cursor();
        cursor.setForward(false);

        Mockito.when(cursorHelperMock.decode(encodedCursor)).thenReturn(cursor);
        final ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        criteria.setForward(false);
        Mockito.when(cursorHelperMock.buildSearchCriteria(cursor, pageSize, null, "ASC")).thenReturn(criteria);
        final List<Article> articles = Arrays.asList(new Article(), new Article());
        Mockito.when(articleRepositoryMock.findByCriteria(criteria)).thenReturn(articles);
        final List<ArticleDto> articleDtos = Arrays.asList(new ArticleDto(), new ArticleDto());
        Mockito.when(articleMapperMock.toArticleDtoList(articles)).thenReturn(articleDtos);
        Mockito.when(cursorHelperMock.buildPrevLink(articleDtos, false, criteria.getSortFieldName(), "ASC")).thenReturn(null);
        Mockito.when(cursorHelperMock.buildNextLink(articleDtos, true, criteria.getSortFieldName(), "ASC")).thenReturn("next");

        CursorResponse<ArticleDto> response = articleService.getByCursor(encodedCursor, pageSize, null, "ASC");

        assertThat(response.getPrev(), is((String) null));
        assertThat(response.getNext(), is("next"));
        Mockito.verify(cursorHelperMock).buildPrevLink(articleDtos, false, criteria.getSortFieldName(), "ASC");
        Mockito.verify(cursorHelperMock).buildNextLink(articleDtos, true, criteria.getSortFieldName(), "ASC");
    }

    @Test
    public void getByCursorFirstPageExactSizeHasNoNext() {
        final int pageSize = 2;

        Mockito.when(cursorHelperMock.decode(null)).thenReturn(null);
        final ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        Mockito.when(cursorHelperMock.buildSearchCriteria(null, pageSize, null, "ASC")).thenReturn(criteria);
        final List<Article> articles = Arrays.asList(new Article(), new Article());
        Mockito.when(articleRepositoryMock.findByCriteria(criteria)).thenReturn(articles);
        final List<ArticleDto> articleDtos = Arrays.asList(new ArticleDto(), new ArticleDto());
        Mockito.when(articleMapperMock.toArticleDtoList(articles)).thenReturn(articleDtos);
        Mockito.when(cursorHelperMock.buildPrevLink(articleDtos, false, criteria.getSortFieldName(), "ASC")).thenReturn(null);
        Mockito.when(cursorHelperMock.buildNextLink(articleDtos, false, criteria.getSortFieldName(), "ASC")).thenReturn(null);

        CursorResponse<ArticleDto> response = articleService.getByCursor(null, pageSize, null, "ASC");

        assertThat(response.getPrev(), is((String) null));
        assertThat(response.getNext(), is((String) null));
        Mockito.verify(cursorHelperMock).buildPrevLink(articleDtos, false, criteria.getSortFieldName(), "ASC");
        Mockito.verify(cursorHelperMock).buildNextLink(articleDtos, false, criteria.getSortFieldName(), "ASC");
    }
}
