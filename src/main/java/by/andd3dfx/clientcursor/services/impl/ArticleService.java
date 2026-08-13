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
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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
        if (!articleRepository.existsById(id)) {
            throw new ArticleNotFoundException(id);
        }
        articleRepository.deleteById(id);
    }

    @Override
    public CursorResponse<ArticleDto> getByCursor(String encodedCursor, Integer pageSize, String sortFieldName, String sortOrder) {
        Cursor cursor = cursorHelper.decode(encodedCursor);
        ArticleSearchCriteria criteria = cursorHelper.buildSearchCriteria(cursor, pageSize, sortFieldName, sortOrder);
        boolean backward = criteria.isBackward();
        criteria.setPageSize(pageSize + 1);
        List<Article> articles = articleRepository.findByCriteria(criteria);

        boolean hasMore = articles.size() > pageSize;
        if (hasMore) {
            articles = trimExtraRecord(articles, backward);
        }

        List<ArticleDto> articleDtos = articleMapper.toArticleDtoList(articles);
        boolean hasPrev = !articleDtos.isEmpty() && (backward ? hasMore : cursor != null);
        boolean hasNext = !articleDtos.isEmpty() && (backward || hasMore);
        String sortOrderName = criteria.getSortOrder().name();
        String prevLink = cursorHelper.buildPrevLink(articleDtos, hasPrev, criteria.getSortFieldName(), sortOrderName);
        String nextLink = cursorHelper.buildNextLink(articleDtos, hasNext, criteria.getSortFieldName(), sortOrderName);
        return new CursorResponse<>(articleDtos, prevLink, nextLink);
    }

    private List<Article> trimExtraRecord(List<Article> articles, boolean backward) {
        if (backward) {
            return new ArrayList<>(articles.subList(1, articles.size()));
        }
        return new ArrayList<>(articles.subList(0, articles.size() - 1));
    }
}
