package by.andd3dfx.templateapp.dto;

import static by.andd3dfx.templateapp.dto.ArticleSearchCriteria.SortOrder.ASC;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSearchCriteria {

    private boolean isForward = true;
    private Long id;
    private Integer pageSize = 50;

    private String sortFieldName;
    private String sortFieldValue;
    private SortOrder sortOrder = ASC;

    public boolean isBackward() {
        return !isForward;
    }

    public enum SortOrder {
        ASC, DESC
    }
}
