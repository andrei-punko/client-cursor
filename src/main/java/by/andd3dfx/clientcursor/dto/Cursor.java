package by.andd3dfx.clientcursor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cursor {

    @Getter(onMethod_ = @JsonProperty("f"))
    @Setter(onMethod_ = @JsonProperty("f"))
    private boolean forward = true;

    @JsonProperty("i")
    private Long id;

    @JsonProperty("n")
    private String sortFieldName;

    @JsonProperty("v")
    private String sortFieldValue;

    @JsonProperty("o")
    private String sortOrder;
}
