package by.andd3dfx.templateapp.services;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleUpdateDto;
import by.andd3dfx.templateapp.dto.CursorResponse;

public interface IArticleService {

    ArticleDto create(ArticleDto articleDto);

    ArticleDto get(Long id);

    void update(Long id, ArticleUpdateDto articleUpdateDto);

    void delete(Long id);

    CursorResponse<ArticleDto> getByCursor(String cursor, Integer pageSize);
}
