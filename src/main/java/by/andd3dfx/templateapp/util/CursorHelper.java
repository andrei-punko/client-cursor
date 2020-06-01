package by.andd3dfx.templateapp.util;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.dto.Cursor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class CursorHelper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public String encode(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        byte[] bytes = objectMapper.writeValueAsBytes(cursor);
        return new String(Base64.getEncoder().encode(bytes));
    }

    @SneakyThrows
    public Cursor decode(String encodedCursor) {
        if (encodedCursor == null) {
            return null;
        }
        byte[] bytes = Base64.getDecoder().decode(encodedCursor);
        return objectMapper.readValue(bytes, Cursor.class);
    }

    public ArticleSearchCriteria buildSearchCriteria(Cursor cursor, Integer pageSize, String sort) {
        validateIncomingParams(cursor, pageSize, sort);

        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        if (cursor != null) {
            criteria.setForward(cursor.isForward());
            criteria.setId(cursor.getId());
            criteria.setSort(cursor.getSortFieldName());
            criteria.setSortFieldValue(cursor.getSortFieldValue());
        } else {
            criteria.setSort(sort);
        }
        criteria.setPageSize(pageSize);
        return criteria;
    }

    private void validateIncomingParams(Cursor cursor, Integer pageSize, String sort) {
        if (cursor != null) {
            if (sort != null) {
                throw new IllegalArgumentException("Sort field name should be set in param OR inside the cursor");
            }

            if (cursor.getSortFieldName() != null && cursor.getSortFieldValue() == null) {
                throw new IllegalArgumentException("Sort field name & value should be populated inside the cursor at the same time");
            }
        }
    }

    @SneakyThrows
    public String buildPrevLink(List<ArticleDto> articles, String sortFieldName) {
        if (articles.isEmpty()) {
            return null;
        }

        ArticleDto firstArticle = articles.get(0);
        Long firstId = firstArticle.getId();
        if (sortFieldName == null) {
            return encode(new Cursor(false, firstId, null, null));
        }

        Field field = firstArticle.getClass().getDeclaredField(sortFieldName);
        field.setAccessible(true);
        String sortFieldValue = String.valueOf(field.get(firstArticle));
        return encode(new Cursor(false, firstId, sortFieldName, sortFieldValue));
    }

    @SneakyThrows
    public String buildNextLink(List<ArticleDto> articles, String sortFieldName) {
        if (articles.isEmpty()) {
            return null;
        }

        ArticleDto lastArticle = articles.get(articles.size() - 1);
        Long lastId = lastArticle.getId();
        if (sortFieldName == null) {
            return encode(new Cursor(true, lastId, null, null));
        }

        Field field = lastArticle.getClass().getDeclaredField(sortFieldName);
        field.setAccessible(true);
        String sortFieldValue = String.valueOf(field.get(lastArticle));
        return encode(new Cursor(true, lastId, sortFieldName, sortFieldValue));
    }
}
