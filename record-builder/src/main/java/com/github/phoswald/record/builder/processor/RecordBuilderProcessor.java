package com.github.phoswald.record.builder.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.github.phoswald.record.builder.RecordBuilder")
public class RecordBuilderProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (element instanceof TypeElement type && isValid(type)) {
                    processRecord(createInfo(type));
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, //
                            "@" + annotation.getSimpleName() + " cannot be used for " + element);
                }
            }
        }
        return true;
    }

    private boolean isValid(TypeElement type) {
        return type.getKind() == ElementKind.RECORD && !type.getModifiers().contains(Modifier.PRIVATE);
    }

    private void processRecord(RecordInfo rec) {
        try {
            String fileName = rec.packageName() + "." + rec.builderName();
            JavaFileObject file = processingEnv.getFiler().createSourceFile(fileName);
            try (Writer writer = file.openWriter()) {
                writer.write(createSourceCode(rec));
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }

    private static String createSourceCode(RecordInfo rec) {
        StringWriter buffer = new StringWriter();
        try (PrintWriter writer = new PrintWriter(buffer)) {
            String modifier = rec.isPublic() ? "public " : "";
            writer.println("package " + rec.packageName() + ";");
            writer.println(modifier + "class " + rec.builderName() + " {");
            for(var comp : rec.components()) {
                writer.println("    private " + comp.type() + " " + comp.name() + ";");
            }
            writer.println("    " + modifier + rec.builderName() + "() { }");
            writer.println("    " + modifier + rec.builderName() + "(" + rec.className() + " inst) {");
            for(var comp : rec.components()) {
                writer.println("        this." + comp.name() + " = inst." + comp.name() + "();");
            }
            writer.println("    }");
            for(var component : rec.components()) {
                writer.println("    " + modifier + rec.builderName() + " " + component.name() + "(" + component.type() + " " + component.name() + ") {");
                writer.println("        this." + component.name() + " = " + component.name() + ";");
                writer.println("        return this;");
                writer.println("    }");
            }
            writer.println("    " + modifier + rec.className() + " build() {");
            writer.println("        return new " + rec.className() + "(" + String.join(", ", rec.componentNames()) + ");");
            writer.println("    }");
            writer.println("}");
        }
        return buffer.toString();
    }

    private static RecordInfo createInfo(TypeElement type) {
        return new RecordInfo( //
                getPackageName(type), //
                getFullClassName(type), //
                type.getSimpleName() + "Builder", //
                type.getModifiers().contains(Modifier.PUBLIC), //
                type.getRecordComponents().stream() //
                        .map(rc -> new RecordComponentInfo(rc.getSimpleName().toString(), rc.asType().toString())) //
                        .toList());
    }

    private static String getPackageName(Element element) {
        if(element instanceof PackageElement pkg) {
            return pkg.getQualifiedName().toString();
        } else if(element != null ) {
            return getPackageName(element.getEnclosingElement());
        } else {
            return "";
        }
    }

    private static String getFullClassName(TypeElement type) {
        String className = type.getSimpleName().toString();
        if(type.getEnclosingElement() instanceof TypeElement parentType) {
            return getFullClassName(parentType) + "." + className;
        } else {
            return className;
        }
    }

    private record RecordInfo( //
            String packageName, //
            String className, //
            String builderName, //
            boolean isPublic, //
            List<RecordComponentInfo> components) {
        private List<String> componentNames() {
            return components().stream().map(RecordComponentInfo::name).toList();
        }
    }

    private record RecordComponentInfo(String name, String type) { }
}
