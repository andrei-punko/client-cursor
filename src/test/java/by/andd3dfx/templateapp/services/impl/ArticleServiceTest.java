package by.andd3dfx.templateapp.services.impl;

import by.andd3dfx.templateapp.dto.*;
import by.andd3dfx.templateapp.exceptions.ArticleNotFoundException;
import by.andd3dfx.templateapp.mappers.ArticleMapper;
import by.andd3dfx.templateapp.persistence.dao.ArticleRepository;
import by.andd3dfx.templateapp.persistence.entities.Article;
import by.andd3dfx.templateapp.util.CursorHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

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

        articleService.update(ARTICLE_ID, articleUpdateDto);

        Mockito.verify(articleRepositoryMock).findById(ARTICLE_ID);
        Mockito.verify(articleMapperMock).toArticle(articleUpdateDto, article);
        Mockito.verify(articleRepositoryMock).save(article);
        Mockito.verify(articleMapperMock).toArticleDto(savedArticle);
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

        articleService.delete(ARTICLE_ID);

        Mockito.verify(articleRepositoryMock).deleteById(ARTICLE_ID);
    }

    @Test
    void deleteAbsentArticle() {
        final Long ARTICLE_ID = 1231L;
        Mockito.doThrow(new EmptyResultDataAccessException(1)).when(articleRepositoryMock).deleteById(ARTICLE_ID);

        try {
            articleService.delete(ARTICLE_ID);

            fail("Exception should be thrown");
        } catch (ArticleNotFoundException ex) {
            Mockito.verify(articleRepositoryMock).deleteById(ARTICLE_ID);
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
        Mockito.when(cursorHelperMock.buildPrevLink(articleDtos, sortFieldName, criteria.getSortFieldName())).thenReturn(prevLink);
        final String nextLink = "some-next-link";
        Mockito.when(cursorHelperMock.buildNextLink(articleDtos, pageSize, criteria.getSortFieldName())).thenReturn(nextLink);

        CursorResponse<ArticleDto> response = articleService
                .getByCursor(encodedCursor, pageSize, sortFieldName, sortOrder);

        assertThat(response.getData(), is(articleDtos));
        assertThat(response.getPrev(), is(prevLink));
        assertThat(response.getNext(), is(nextLink));

        Mockito.verify(cursorHelperMock).decode(encodedCursor);
        Mockito.verify(cursorHelperMock).buildSearchCriteria(cursor, pageSize, sortFieldName, sortOrder);
        Mockito.verify(articleRepositoryMock).findByCriteria(criteria);
        Mockito.verify(articleMapperMock).toArticleDtoList(articles);
        Mockito.verify(cursorHelperMock).buildPrevLink(articleDtos, sortFieldName, criteria.getSortFieldName());
        Mockito.verify(cursorHelperMock).buildNextLink(articleDtos, pageSize, criteria.getSortFieldName());
    }
}
