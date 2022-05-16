package com.generate.dynamic;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author derrick
 */
public class ByteArrayFileManager extends ForwardingJavaFileManager {
    public static final Map<String, byte[]> CLASS_BYTE_MAP = new HashMap<>(16);

    protected ByteArrayFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
            FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            return new ClassOutputBuffer(className, sibling.toUri(), kind);
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    private static class ClassOutputBuffer extends SimpleJavaFileObject {
        private final String className;

        protected ClassOutputBuffer(String className, URI uri, Kind kind) {
            super(uri, kind);
            this.className = className;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) out;
                    CLASS_BYTE_MAP.put(className, byteArrayOutputStream.toByteArray());
                }
            };
        }
    }

}
