package by.andd3dfx.clientcursor.services;

import by.andd3dfx.clientcursor.dto.ArticleDto;
import by.andd3dfx.clientcursor.dto.ArticleUpdateDto;
import by.andd3dfx.clientcursor.dto.CursorResponse;

public interface IArticleService {

    ArticleDto create(ArticleDto articleDto);

    ArticleDto get(Long id);

    ArticleDto update(Long id, ArticleUpdateDto articleUpdateDto);

    void delete(Long id);

    CursorResponse<ArticleDto> getByCursor(String cursor, Integer pageSize, String sortFieldName, String sortOrder);
}
