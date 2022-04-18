package by.andd3dfx.templateapp.controllers;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleUpdateDto;
import by.andd3dfx.templateapp.dto.CursorResponse;
import by.andd3dfx.templateapp.services.IArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleControllerTest {

    @InjectMocks
    private ArticleController articleController;

    @Mock
    private IArticleService articleService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createArticle() {
        final ArticleDto articleDto = buildArticleDto();
        final ArticleDto createdArticle = buildArticleDto();
        createdArticle.setId(123L);
        when(articleService.create(articleDto)).thenReturn(createdArticle);

        ArticleDto result = articleController.createArticle(articleDto);

        assertThat(result, is(createdArticle));
        verify(articleService).create(articleDto);
    }

    @Test
    void readArticle() {
        final Long articleId = 123L;
        final ArticleDto article = buildArticleDto();
        when(articleService.get(articleId)).thenReturn(article);

        ArticleDto result = articleController.readArticle(articleId);

        assertThat(result, is(article));
        verify(articleService).get(articleId);
    }

    @Test
    void updateArticle() {
        final Long articleId = 123L;
        ArticleUpdateDto articleUpdateDto = buildArticleUpdateDto();

        articleController.updateArticle(articleId, articleUpdateDto);

        verify(articleService).update(articleId, articleUpdateDto);
    }

    @Test
    void deleteArticle() {
        final Long articleId = 123L;

        articleController.deleteArticle(articleId);

        verify(articleService).delete(articleId);
    }

    @Test
    void getArticlesByCursor() {
        final String cursor = "bla-bla-cursor";
        final String sortFieldName = "title";
        final String sortOrder = "ASC";
        final Integer pageSize = 50;
        CursorResponse<ArticleDto> cursorResponse = new CursorResponse<>();
        cursorResponse.setData(Arrays.asList(buildArticleDto()));
        when(articleService.getByCursor(cursor, pageSize, sortFieldName, sortOrder)).thenReturn(cursorResponse);

        CursorResponse<ArticleDto> result = articleController.getArticlesByCursor(cursor, sortFieldName, sortOrder, pageSize);

        assertThat(result, is(cursorResponse));
        verify(articleService).getByCursor(cursor, pageSize, sortFieldName, sortOrder);
    }

    private ArticleDto buildArticleDto() {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("Some tittle value");
        articleDto.setSummary("Some summary value");
        articleDto.setText("Some text");
        return articleDto;
    }

    private ArticleUpdateDto buildArticleUpdateDto() {
        ArticleUpdateDto articleUpdateDto = new ArticleUpdateDto();
        articleUpdateDto.setTitle("Some weird title");
        articleUpdateDto.setText("Some text");
        articleUpdateDto.setSummary("Summary");
        return articleUpdateDto;
    }
}
