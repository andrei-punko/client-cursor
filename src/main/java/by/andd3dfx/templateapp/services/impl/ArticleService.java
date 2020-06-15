package by.andd3dfx.templateapp.services.impl;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.dto.ArticleUpdateDto;
import by.andd3dfx.templateapp.dto.Cursor;
import by.andd3dfx.templateapp.dto.CursorResponse;
import by.andd3dfx.templateapp.exceptions.ArticleNotFoundException;
import by.andd3dfx.templateapp.mappers.ArticleMapper;
import by.andd3dfx.templateapp.persistence.dao.ArticleRepository;
import by.andd3dfx.templateapp.persistence.entities.Article;
import by.andd3dfx.templateapp.services.IArticleService;
import by.andd3dfx.templateapp.util.CursorHelper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService implements IArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final CursorHelper cursorHelper;

    @Transactional
    @Override
    public ArticleDto create(ArticleDto articleDto) {
        Article entity = articleMapper.toArticle(articleDto);
        Article savedEntity = articleRepository.save(entity);
        return articleMapper.toArticleDto(savedEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public ArticleDto get(Long id) {
        return articleRepository.findById(id)
            .map(articleMapper::toArticleDto)
            .orElseThrow(() -> new ArticleNotFoundException(id));
    }

    @Transactional
    @Override
    public void update(Long id, ArticleUpdateDto articleUpdateDto) {
        articleRepository.findById(id)
            .map(article -> {
                articleMapper.toArticle(articleUpdateDto, article);
                Article savedArticle = articleRepository.save(article);
                return articleMapper.toArticleDto(savedArticle);
            }).orElseThrow(() -> new ArticleNotFoundException(id));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public CursorResponse<ArticleDto> getByCursor(String encodedCursor, Integer pageSize, String sortFieldName, String sortOrder) {
        Cursor cursor = cursorHelper.decode(encodedCursor);
        ArticleSearchCriteria criteria = cursorHelper.buildSearchCriteria(cursor, pageSize, sortFieldName, sortOrder);
        List<Article> articles = articleRepository.findByCriteria(criteria);

        List<ArticleDto> articleDtos = articleMapper.toArticleDtoList(articles);
        String prevLink = cursorHelper.buildPrevLink(articleDtos, sortFieldName, criteria.getSortFieldName());
        String nextLink = cursorHelper.buildNextLink(articleDtos, pageSize, criteria.getSortFieldName());
        return new CursorResponse<>(articleDtos, prevLink, nextLink);
    }
}
