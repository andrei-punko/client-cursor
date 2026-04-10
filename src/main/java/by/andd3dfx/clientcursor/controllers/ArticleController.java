package by.andd3dfx.clientcursor.controllers;

import by.andd3dfx.clientcursor.dto.ArticleDto;
import by.andd3dfx.clientcursor.dto.ArticleUpdateDto;
import by.andd3dfx.clientcursor.dto.CursorResponse;
import by.andd3dfx.clientcursor.services.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
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

    @Operation(summary = "Create new article")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Article successfully created"),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDto createArticle(
        @Parameter(description = "New article's data")
        @Validated
        @RequestBody ArticleDto newArticleDto
    ) {
        return articleService.create(newArticleDto);
    }

    @Operation(summary = "Get article by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Article successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Article not found"),
    })
    @GetMapping("/{id}")
    public ArticleDto readArticle(
        @Parameter(description = "Article's id")
        @NotNull
        @PathVariable Long id
    ) {
        return articleService.get(id);
    }

    @Operation(summary = "Update article")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Article successfully updated"),
        @ApiResponse(responseCode = "404", description = "Article not found"),
    })
    @PatchMapping("/{id}")
    public ArticleDto updateArticle(
        @Parameter(description = "Article's id")
        @NotNull
        @PathVariable Long id,
        @Parameter(description = "Updated fields of article")
        @Validated
        @RequestBody ArticleUpdateDto articleUpdateDto
    ) {
        return articleService.update(id, articleUpdateDto);
    }

    @Operation(summary = "Delete article by id")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Article successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Article not found"),
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(
        @Parameter(description = "Article's id")
        @NotNull
        @PathVariable Long id
    ) {
        articleService.delete(id);
    }

    @Operation(summary = "Read articles by cursor")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Articles successfully retrieved"),
    })
    @GetMapping
    public CursorResponse<ArticleDto> getArticlesByCursor(
        @Parameter(description = "Encoded cursor")
        @RequestParam(required = false) String cursor,
        @RequestParam(value = "sort", required = false) String sortFieldName,
        @RequestParam(value = "order", required = false, defaultValue = "ASC") String sortOrder,
        @RequestParam(value = "size", defaultValue = "50", required = false) Integer pageSize
    ) {
        return articleService.getByCursor(cursor, pageSize, sortFieldName, sortOrder);
    }
}
