package com.dfi.sbc2ha.helper.ha.command;

import com.dfi.sbc2ha.Easing;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LightCommand {

    int brightness;
    @JsonProperty("color_mode")
    String colorMode;
    @JsonProperty("color_temp")
    int colorTemp;
    ColorCommand color;
    Easing effect;
    String state;
    int transition;
}
