package by.andd3dfx.templateapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cursor {

    @JsonProperty("f")
    private boolean isForward = true;

    @JsonProperty("i")
    private Long id;

    @JsonProperty("n")
    private String sortFieldName;

    @JsonProperty("v")
    private String sortFieldValue;
}
