package by.andd3dfx.clientcursor.services.impl;

import by.andd3dfx.clientcursor.dto.ArticleDto;
import by.andd3dfx.clientcursor.dto.ArticleSearchCriteria;
import by.andd3dfx.clientcursor.dto.ArticleUpdateDto;
import by.andd3dfx.clientcursor.dto.Cursor;
import by.andd3dfx.clientcursor.dto.CursorResponse;
import by.andd3dfx.clientcursor.exceptions.ArticleNotFoundException;
import by.andd3dfx.clientcursor.mappers.ArticleMapper;
import by.andd3dfx.clientcursor.persistence.dao.ArticleRepository;
import by.andd3dfx.clientcursor.persistence.entities.Article;
import by.andd3dfx.clientcursor.services.IArticleService;
import by.andd3dfx.clientcursor.util.CursorHelper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
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
    public ArticleDto update(Long id, ArticleUpdateDto articleUpdateDto) {
        return articleRepository.findById(id)
            .map(article -> {
                articleMapper.toArticle(articleUpdateDto, article);
                Article savedArticle = articleRepository.save(article);
                return articleMapper.toArticleDto(savedArticle);
            }).orElseThrow(() -> new ArticleNotFoundException(id));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        try {
            articleRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ex) {
            throw new ArticleNotFoundException(id);
        }
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
