package com.dfi.sbc2ha.config.sbc2ha.definition.extentsionBoard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExtensionBoardsConfig {
    String vendor;
    @JsonProperty("input_board")
    String inputBoard;
    @JsonProperty("output_board")
    String outputBoard;
}
