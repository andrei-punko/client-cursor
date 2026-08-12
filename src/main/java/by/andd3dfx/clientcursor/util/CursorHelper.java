package by.andd3dfx.clientcursor.util;

import by.andd3dfx.clientcursor.dto.ArticleDto;
import by.andd3dfx.clientcursor.dto.ArticleSearchCriteria;
import by.andd3dfx.clientcursor.dto.ArticleSearchCriteria.SortOrder;
import by.andd3dfx.clientcursor.dto.Cursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class CursorHelper {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int MAX_PAGE_SIZE = 1000;

    private static final Map<String, Function<ArticleDto, String>> SORT_FIELDS = Map.of(
        "title", ArticleDto::getTitle,
        "author", ArticleDto::getAuthor,
        "summary", ArticleDto::getSummary
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String encode(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(cursor);
            return new String(Base64.getEncoder().encode(bytes), DEFAULT_ENCODING);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error during encoding", ex);
        }
    }

    public Cursor decode(String encodedCursor) {
        if (encodedCursor == null) {
            return null;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(encodedCursor.getBytes(DEFAULT_ENCODING));
            return objectMapper.readValue(bytes, Cursor.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error during decoding", ex);
        }
    }

    public ArticleSearchCriteria buildSearchCriteria(Cursor cursor, Integer pageSize, String sortFieldName, String sortOrder) {
        validateIncomingParams(cursor, pageSize, sortFieldName);

        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        if (cursor != null) {
            criteria.setForward(cursor.isForward());
            criteria.setId(cursor.getId());
            criteria.setSortFieldValue(cursor.getSortFieldValue());
        }
        criteria.setSortFieldName(resolveSortFieldName(cursor, sortFieldName));
        criteria.setSortOrder(resolveAndValidateSortOrder(cursor, sortOrder));
        criteria.setPageSize(pageSize);
        return criteria;
    }

    private String resolveSortFieldName(Cursor cursor, String explicitSortFieldName) {
        if (cursor == null) {
            return explicitSortFieldName;
        }
        return cursor.getSortFieldName();
    }

    private SortOrder resolveAndValidateSortOrder(Cursor cursor, String explicitSortOrder) {
        String order = (cursor == null) ? explicitSortOrder : cursor.getSortOrder();
        if (order == null) {
            return SortOrder.ASC;
        }

        try {
            return SortOrder.valueOf(order);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported sort order: '" + order + "'. Allowed: ASC, DESC");
        }
    }

    private void validateIncomingParams(Cursor cursor, Integer pageSize, String sortFieldName) {
        validatePageSize(pageSize);
        if (cursor != null) {
            if (sortFieldName != null) {
                throw new IllegalArgumentException("Sort field name should be set in param OR inside the cursor");
            }

            if (cursor.getSortFieldName() != null && cursor.getSortFieldValue() == null) {
                throw new IllegalArgumentException("Sort field name & value should be populated inside the cursor at the same time");
            }

            validateSortFieldName(cursor.getSortFieldName());
        } else {
            validateSortFieldName(sortFieldName);
        }
    }

    private void validatePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be a positive integer");
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be greater than " + MAX_PAGE_SIZE);
        }
    }

    private void validateSortFieldName(String sortFieldName) {
        if (sortFieldName == null) {
            return;
        }
        if (!SORT_FIELDS.containsKey(sortFieldName)) {
            String allowed = String.join(", ", SORT_FIELDS.keySet());
            throw new IllegalArgumentException("Unsupported sort field: '" + sortFieldName + "'. Allowed: " + allowed);
        }
    }

    public String buildPrevLink(List<ArticleDto> articles, String explicitSort, String sortFieldName, String sortOrder) {
        if (articles.isEmpty() || explicitSort != null) {
            return null;
        }

        ArticleDto firstArticle = articles.getFirst();
        Long firstId = firstArticle.getId();
        return encode(new Cursor(false, firstId, sortFieldName, extractSortFieldValue(sortFieldName, firstArticle), sortOrder));
    }


    public String buildNextLink(List<ArticleDto> articles, Integer pageSize, String sortFieldName, String sortOrder) {
        if (articles.isEmpty() || articles.size() < pageSize) {
            return null;
        }

        ArticleDto lastArticle = articles.getLast();
        Long lastId = lastArticle.getId();
        return encode(new Cursor(true, lastId, sortFieldName, extractSortFieldValue(sortFieldName, lastArticle), sortOrder));
    }

    private String extractSortFieldValue(String sortFieldName, ArticleDto article) {
        if (sortFieldName == null) {
            return null;
        }
        validateSortFieldName(sortFieldName);
        return SORT_FIELDS.get(sortFieldName).apply(article);
    }
}
