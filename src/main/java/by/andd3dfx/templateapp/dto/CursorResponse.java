package by.andd3dfx.templateapp.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorResponse<T> {

    private List<T> data;
    private String prev;
    private String next;
}
