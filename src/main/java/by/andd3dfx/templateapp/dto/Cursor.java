package by.andd3dfx.templateapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cursor {

    private boolean isForward = true;
    private Long id;
}
