package cn.ksmcbrigade.mp.utils;

import cn.ksmcbrigade.mp.MPAgent;
import org.apache.commons.io.FileUtils;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompileUtils {

    public static File compile(File file) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,fileManager,null,
                List.of("-proc:none",
                        "-d","mp-classes")
                ,null,fileManager.getJavaFileObjects(file));
        task.call();
        return getCompiledFile(file);
    }

    public static File getCompiledFile(File file) throws IOException {
        return new File(
                "mp-classes/"+getPackageName(file).replace(".","/")
                        +"/"+file.getName().split("\\.")[0]+".class");
    }

    public static String getPackageName(File file) throws IOException {
        return extractPackageName(FileUtils.readFileToString(file));
    }

    public static String getFullClassName(byte[] sourceFile) {
        String content = new String(sourceFile);
        String packageName = extractPackageName(content);
        String className = extractClassName(content);

        if (className == null) {
            throw new IllegalArgumentException("Can not find the class name.");
        }

        if (packageName.isEmpty()) {
            return className;
        } else {
            return packageName + "." + className;
        }
    }

    private static String extractPackageName(String content) {
        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile("package\\s+([a-zA-Z0-9_.]+)\\s*;");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String extractClassName(String content) {
        Pattern classPattern = Pattern.compile(
                "(?:public\\s+)?(?:class|interface|enum|record)\\s+([a-zA-Z0-9_]+)" +
                        "(?:<[^>]*>)?(?:\\s+extends\\s+[^{]+)?(?:\\s+implements\\s+[^{]+)?\\s*\\{",
                Pattern.DOTALL
        );

        Matcher matcher = classPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        Pattern simplePattern = Pattern.compile(
                "class\\s+([a-zA-Z0-9_]+)"
        );
        matcher = simplePattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static void decompileThread(File file,File toFile){
        new Thread(()->{
            try {
                File cfrTool = new File("cfr.jar");
                File javaw = new File(System.getProperty("java.home")+"/bin/javaw");
                if(!cfrTool.exists()){
                    MPAgent.LOGGER.error("{} not found.",cfrTool);
                }
                else{
                    ProcessBuilder builder = new ProcessBuilder(javaw.getAbsolutePath(),
                            "-jar",
                            cfrTool.getAbsolutePath(),
                            file.getAbsolutePath(),
                            "--outputdir",
                            toFile.getParentFile().getAbsolutePath()
                    );
                    builder.inheritIO().start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
