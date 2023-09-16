package com.dfi.sbc2ha.web.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

@JsonIncludeProperties(value = {"name"})
abstract public class ThreadMixin {
    @JsonIgnore
    abstract int ignoreThis(); // we don't need it!

}