package com.generate.dynamic;

import com.generate.properties.ParameterProperty;
import com.generate.properties.StructureProperty;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author derrick
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DynamicClassLoader extends ClassLoader {
    private final Set<File> compileFiles = new HashSet<>();
    private final StructureProperty structureProperty;
    private final ParameterProperty parameterProperty;
    private final Map<String, byte[]> classByteMap = new HashMap<>(16);
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] buf = this.classByteMap.get(name);
        if (buf == null) {
            return super.findClass(name);
        }

        return defineClass(name, buf, 0, buf.length);
    }

    public void compile() throws IOException {
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null);
        if (parameterProperty.isOutput()) {

            OutputFileManager manager = new OutputFileManager(standardFileManager);
            File classesDir = new File(structureProperty.getCompileOut() + "/");
            if (classesDir.exists() || classesDir.mkdirs()) {
                standardFileManager.setLocation(StandardLocation.CLASS_OUTPUT, Lists.newArrayList(classesDir));
            }
            Iterable<? extends JavaFileObject> fileObjects = standardFileManager.getJavaFileObjectsFromFiles(
                    this.compileFiles);
            List<String> ops = new ArrayList<>();
            ops.add("-Xlint:unchecked");
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, fileObjects);
            if (task.call()) {
                for (String className : OutputFileManager.CLASS_PATH_MAP.keySet()) {
                    classByteMap.put(className, Files.readAllBytes(OutputFileManager.CLASS_PATH_MAP.get(className)));
                }
            }
            standardFileManager.close();
            manager.close();
        } else {

            ByteArrayFileManager manager = new ByteArrayFileManager(standardFileManager);
            Iterable<? extends JavaFileObject> fileObjects = standardFileManager.getJavaFileObjectsFromFiles(
                    this.compileFiles);
            List<String> ops = new ArrayList<>();
            ops.add("-Xlint:unchecked");
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, ops, null, fileObjects);
            if (task.call()) {
                classByteMap.putAll(ByteArrayFileManager.CLASS_BYTE_MAP);
            }
            standardFileManager.close();
            manager.close();
        }
    }


    public boolean notCompiled(String name) {
        return !classByteMap.containsKey(name);
    }

    public void addCompileFile(File file) {
        this.compileFiles.add(file);
    }
}
