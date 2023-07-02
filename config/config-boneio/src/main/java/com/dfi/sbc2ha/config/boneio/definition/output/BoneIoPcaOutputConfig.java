package com.dfi.sbc2ha.config.boneio.definition.output;


import com.dfi.sbc2ha.config.boneio.definition.enums.OutputKindType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BoneIoPcaOutputConfig extends BoneIoOutputConfig {

    @JsonProperty("pca_id")
    String pcaId;

    @JsonProperty("percentage_default_brightness")
    int percentageDefaultBrightnes = 1;

    public BoneIoPcaOutputConfig() {
        super();
        kind = OutputKindType.PCA;
    }
}
