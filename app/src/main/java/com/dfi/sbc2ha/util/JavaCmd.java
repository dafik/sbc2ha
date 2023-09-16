package com.dfi.sbc2ha.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JavaCmd {
    public static List<String> getCommand(String[] args)  {
        String[] args1;
        if (args.length == 0) {
            return null;
        } else {
            args1 = Arrays.copyOf(args, args.length );
        }

        List<String> command = new ArrayList<>(32);
        appendJavaExecutable(command);
        //appendVMArgs(command);
        appendClassPath(command);
        appendEntryPoint(command);
        appendArgs(command, args1);

        return command;

    }

    private static void appendJavaExecutable(List<String> cmd) {
        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
    }

    private static void appendVMArgs(Collection<String> cmd) {
        Collection<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        String javaToolOptions = System.getenv("JAVA_TOOL_OPTIONS");
        if (javaToolOptions != null) {
            Collection<String> javaToolOptionsList = Arrays.asList(javaToolOptions.split(" "));
            vmArguments = new ArrayList<>(vmArguments);
            vmArguments.removeAll(javaToolOptionsList);
        }

        cmd.addAll(vmArguments);
    }

    private static void appendClassPath(List<String> cmd) {
        cmd.add("-cp");
        cmd.add(ManagementFactory.getRuntimeMXBean().getClassPath());
    }

    private static void appendEntryPoint(List<String> cmd) {
        StackTraceElement[] stackTrace          = new Throwable().getStackTrace();
        StackTraceElement   stackTraceElement   = stackTrace[stackTrace.length - 1];
        String              fullyQualifiedClass = stackTraceElement.getClassName();
        String              entryMethod         = stackTraceElement.getMethodName();
        if (!entryMethod.equals("main"))
            throw new AssertionError("Entry point is not a 'main()': " + fullyQualifiedClass + '.' + entryMethod);

        cmd.add(fullyQualifiedClass);
    }

    private static void appendArgs(List<String> cmd, String[] args) {
        List<String> list = Arrays.asList(args);
        cmd.addAll(list);
    }
}
