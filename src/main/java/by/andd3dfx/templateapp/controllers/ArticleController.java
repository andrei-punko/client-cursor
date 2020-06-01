package by.andd3dfx.templateapp.controllers;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleUpdateDto;
import by.andd3dfx.templateapp.dto.CursorResponse;
import by.andd3dfx.templateapp.services.IArticleService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/articles")
public class ArticleController {

    private final IArticleService articleService;

    @ApiOperation(value = "Create new article", response = ArticleDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Article successfully created"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDto createArticle(
        @ApiParam("New article's data")
        @Validated
        @RequestBody ArticleDto newArticleDto
    ) {
        return articleService.create(newArticleDto);
    }

    @ApiOperation(value = "Get article by id", response = ArticleDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Article successfully retrieved"),
        @ApiResponse(code = 404, message = "Article not found"),
    })
    @GetMapping("/{id}")
    public ArticleDto readArticle(
        @ApiParam("Article's id")
        @NotNull
        @PathVariable Long id
    ) {
        return articleService.get(id);
    }

    @ApiOperation("Update article")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Article successfully updated"),
        @ApiResponse(code = 404, message = "Article not found"),
    })
    @PatchMapping("/{id}")
    public void updateArticle(
        @ApiParam("Article's id")
        @NotNull
        @PathVariable Long id,
        @ApiParam("Updated fields of article")
        @Validated
        @RequestBody ArticleUpdateDto articleUpdateDto
    ) {
        articleService.update(id, articleUpdateDto);
    }

    @ApiOperation("Delete article by id")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Article successfully deleted"),
        @ApiResponse(code = 404, message = "Article not found"),
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(
        @ApiParam("Article's id")
        @NotNull
        @PathVariable Long id
    ) {
        articleService.delete(id);
    }

    @ApiOperation(value = "Read articles by cursor", response = CursorResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Articles successfully retrieved"),
    })
    @GetMapping
    public CursorResponse<ArticleDto> getArticlesByCursor(
        @ApiParam("Encoded cursor")
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "50", required = false) Integer pageSize
    ) {
        return articleService.getByCursor(cursor, pageSize, sort);
    }
}
