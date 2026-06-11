package dev.sim0n.caesium;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import dev.sim0n.caesium.exception.CaesiumException;
import dev.sim0n.caesium.gui.LibraryTab;
import dev.sim0n.caesium.util.classwriter.ClassTree;
import dev.sim0n.caesium.util.wrapper.impl.ClassWrapper;

public class PreRuntime {
    private static Map<String, ClassWrapper> classPath = new HashMap<>();
    private static Map<String, ClassWrapper> classes = new HashMap<>();
    private static Map<String, ClassTree> hierarchy = new HashMap<>();
    public static DefaultListModel<String> libraries = new DefaultListModel<>();

    public static void loadJavaRuntime() throws HeadlessException, IOException {
        try {
            FileSystem jrtFs = FileSystems.getFileSystem(URI.create("jrt:/"));
            Path modulesRoot = jrtFs.getPath("/modules");
            try (Stream<Path> modules = Files.list(modulesRoot)) {
                modules.forEach(modulePath -> {
                    libraries.addElement(modulePath.toUri().toString());
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Could not load Java runtime via jrt:/ filesystem.\n" + e.getMessage(),
                    "Runtime Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void loadClassPath() {
        ArrayList<String> libs = Collections.list(libraries.elements());

        for (String s : libs) {
            if (s.startsWith("jrt:/")) {
                loadJrtModule(s);
            } else {
                loadJarFile(new File(s));
            }
        }
    }
    
    private static void loadJrtModule(String jrtUri) {
        try {
            FileSystem jrtFs = FileSystems.getFileSystem(URI.create("jrt:/"));
            String moduleName = URI.create(jrtUri).getPath().replaceFirst("^/modules/", "").replaceFirst("^/", "");
            Path modulePath = jrtFs.getPath("/modules/" + moduleName);
            try (Stream<Path> walk = Files.walk(modulePath)) {
                walk.filter(p -> p.toString().endsWith(".class")).forEach(classFile -> {
                    try {
                        byte[] bytes = Files.readAllBytes(classFile);
                        ClassReader cr = new ClassReader(bytes);
                        ClassNode classNode = new ClassNode();
                        cr.accept(classNode,
                                ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                        ClassWrapper classWrapper = new ClassWrapper(classNode, true);
                        classPath.put(classWrapper.originalName, classWrapper);
                    } catch (Throwable ignored) {
                     }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadJarFile(File file) {
        if (!file.exists()) return;

        System.out.printf("Loading library \"%s\".%n", file.getAbsolutePath());
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    try {
                        ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                        ClassNode classNode = new ClassNode();
                        cr.accept(classNode,
                                ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                        ClassWrapper classWrapper = new ClassWrapper(classNode, true);
                        classPath.put(classWrapper.originalName, classWrapper);
                    } catch (Throwable ignored) {
                        // Don't care.
                    }
                }
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadInput(String inputFile) throws CaesiumException {
        File input = new File(inputFile);
        if (input.exists()) {
            try (ZipFile zipFile = new ZipFile(input)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!entry.isDirectory()) {
                        if (entry.getName().endsWith(".class")) {
                            try {
                                ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
                                ClassNode classNode = new ClassNode();
                                cr.accept(classNode, ClassReader.SKIP_FRAMES);
                                ClassWrapper classWrapper = new ClassWrapper(classNode, false);
                                classPath.put(classWrapper.originalName, classWrapper);
                                classes.put(classWrapper.originalName, classWrapper);
                            } catch (Throwable ignored) {
                            }
                        }
                    }
                }
            } catch (ZipException e) {
                throw new CaesiumException(
                        String.format("Input file \"%s\" could not be opened as a zip file.", input.getAbsolutePath()),
                        e);
            } catch (IOException e) {
                throw new CaesiumException(String.format(
                        "IOException happened while trying to load classes from \"%s\".", input.getAbsolutePath()), e);
            }
        } else {
            throw new CaesiumException(String.format("Unable to find \"%s\".", input.getAbsolutePath()), null);
        }
    }

    public static void buildHierarchy(ClassWrapper classWrapper, ClassWrapper sub) throws CaesiumException {
        if (hierarchy.get(classWrapper.node.name) == null) {
            ClassTree tree = new ClassTree(classWrapper);
            if (classWrapper.node.superName != null) {
                tree.parentClasses.add(classWrapper.node.superName);
                ClassWrapper superClass = classPath.get(classWrapper.node.superName);
                if (superClass == null) {
                    hierarchy.put(classWrapper.node.name, tree);
                    if (sub != null) {
                        hierarchy.get(classWrapper.node.name).subClasses.add(sub.node.name);
                    }
                    return;
                }
                buildHierarchy(superClass, classWrapper);
            }
            if (classWrapper.node.interfaces != null && !classWrapper.node.interfaces.isEmpty()) {
                for (String s : classWrapper.node.interfaces) {
                    tree.parentClasses.add(s);
                    ClassWrapper interfaceClass = classPath.get(s);
                    if (interfaceClass == null)
                        continue;
                    buildHierarchy(interfaceClass, classWrapper);
                }
            }
            hierarchy.put(classWrapper.node.name, tree);
        }
        if (sub != null) {
            hierarchy.get(classWrapper.node.name).subClasses.add(sub.node.name);
        }
    }

    public static void buildInheritance() {
        classes.values().forEach(classWrapper -> {
            try {
                buildHierarchy(classWrapper, null);
            } catch (CaesiumException e) {
                e.printStackTrace();
            }
        });
    }

    public static Map<String, ClassWrapper> getClassPath() {
        return classPath;
    }

    public static Map<String, ClassWrapper> getClasses() {
        return classes;
    }

    public static Map<String, ClassTree> getHierarchy() {
        return hierarchy;
    }
}
