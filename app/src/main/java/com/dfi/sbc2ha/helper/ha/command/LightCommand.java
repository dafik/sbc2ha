package com.dfi.sbc2ha.helper.ha.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LightCommand {
    public static final String ON = "ON";
    public static final String OFF = "OFF";

    int brightness;
    @JsonProperty("color_mode")
    String colorMode;
    @JsonProperty("color_temp")
    int colorTemp;
    ColorCommand color;
    String effect;
    String state;
    int transition;
}
