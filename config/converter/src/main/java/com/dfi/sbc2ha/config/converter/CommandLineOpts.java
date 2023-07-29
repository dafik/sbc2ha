package com.dfi.sbc2ha.config.converter;

import com.beust.jcommander.Parameter;

public class CommandLineOpts {
    @Parameter(description = "source config file", required = true)
    public String configFile;

    @Parameter(names = { "-v", "--vendor" }, description = "vendor of configuration eg. boneio")
    public String vendor = "boneio";

    @Parameter(names = {"-i", "--input"}, description = "version of input board")
    public String inputBoard;

    @Parameter(names = {"-o", "--output"}, description = "version of output board")
    public String outputBoard;

}
