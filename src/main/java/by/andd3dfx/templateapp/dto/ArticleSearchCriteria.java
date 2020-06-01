package by.andd3dfx.templateapp.dto;

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

    public boolean isBackward() {
        return !isForward;
    }
}
