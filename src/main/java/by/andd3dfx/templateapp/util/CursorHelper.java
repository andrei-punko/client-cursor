package by.andd3dfx.templateapp.util;

import by.andd3dfx.templateapp.dto.ArticleDto;
import by.andd3dfx.templateapp.dto.ArticleSearchCriteria;
import by.andd3dfx.templateapp.dto.Cursor;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        ArticleSearchCriteria criteria = new ArticleSearchCriteria();
        if (cursor != null) {
            criteria.setForward(cursor.isForward());
            criteria.setId(cursor.getId());
        }
        criteria.setPageSize(pageSize);
        criteria.setSort(sort);
        return criteria;
    }

    public String buildPrevLink(List<ArticleDto> articles) {
        if (articles.isEmpty()) {
            return null;
        }

        Long firstId = articles.get(0).getId();
        return encode(new Cursor(false, firstId));
    }

    public String buildNextLink(List<ArticleDto> articles) {
        if (articles.isEmpty()) {
            return null;
        }

        Long lastId = articles.get(articles.size() - 1).getId();
        return encode(new Cursor(true, lastId));
    }
}
