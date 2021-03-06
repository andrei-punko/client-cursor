package by.andd3dfx.templateapp.util;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria.SortOrder;
import by.andd3dfx.templateapp.dto.Cursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CursorHelper {

    public static final String DEFAULT_ENCODING = "UTF-8";
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
        criteria.setSortOrder(SortOrder.valueOf(resolveSortOrder(cursor, sortOrder)));
        criteria.setPageSize(pageSize);
        return criteria;
    }

    private String resolveSortFieldName(Cursor cursor, String explicitSortFieldName) {
        if (cursor == null) {
            return explicitSortFieldName;
        }
        return cursor.getSortFieldName();
    }

    private String resolveSortOrder(Cursor cursor, String explicitSortOrder) {
        if (cursor == null) {
            return explicitSortOrder;
        }
        return cursor.getSortOrder();
    }

    private void validateIncomingParams(Cursor cursor, Integer pageSize, String sortFieldName) {
        if (cursor != null) {
            if (sortFieldName != null) {
                throw new IllegalArgumentException("Sort field name should be set in param OR inside the cursor");
            }

            if (cursor.getSortFieldName() != null && cursor.getSortFieldValue() == null) {
                throw new IllegalArgumentException("Sort field name & value should be populated inside the cursor at the same time");
            }
        }
    }

    public String buildPrevLink(List<ArticleDto> articles, String explicitSort, String sortFieldName) {
        if (articles.isEmpty() || explicitSort != null) {
            return null;
        }

        ArticleDto firstArticle = articles.get(0);
        Long firstId = firstArticle.getId();
        return encode(new Cursor(false, firstId, sortFieldName, extractSortFieldValue(sortFieldName, firstArticle), "ASC"));
    }


    public String buildNextLink(List<ArticleDto> articles, Integer pageSize, String sortFieldName) {
        if (articles.isEmpty() || articles.size() < pageSize) {
            return null;
        }

        ArticleDto lastArticle = articles.get(articles.size() - 1);
        Long lastId = lastArticle.getId();
        return encode(new Cursor(true, lastId, sortFieldName, extractSortFieldValue(sortFieldName, lastArticle), "ASC"));
    }

    private String extractSortFieldValue(String sortFieldName, ArticleDto firstArticle) {
        if (sortFieldName == null) {
            return null;
        }
        try {
            Field field = firstArticle.getClass().getDeclaredField(sortFieldName);
            field.setAccessible(true);
            return String.valueOf(field.get(firstArticle));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Error during sort field value extraction", ex);
        }
    }
}
