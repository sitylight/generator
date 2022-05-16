package com.generate.dynamic;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author derrick
 */
public class OutputFileManager extends ForwardingJavaFileManager {
    public static final Map<String, Path> CLASS_PATH_MAP = new HashMap<>(16);
    private String classesOutputLocation = null;

    /**
     * Creates a new instance of ForwardingJavaFileManager.
     *
     * @param fileManager delegate to this file manager
     */
    protected OutputFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }




    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
            FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            JavaFileObject fileObject = super.getJavaFileForOutput(location, className, kind, sibling);
            if (classesOutputLocation == null) {
                classesOutputLocation = ((StandardJavaFileManager) this.fileManager).getLocation(location).iterator().next()
                        .getPath();
            }
//            String classesOutput = ;
//            String classesOutput = "/Users/derrick/cityline/ai-sec/ai-sec-api";
            String classFilePath = classesOutputLocation + "/" + this.inferBinaryName(location, fileObject).replace(".", "/")
                    + ".class";
            Path path = Paths.get(classFilePath);
            CLASS_PATH_MAP.put(className, path);
            return fileObject;
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

}
