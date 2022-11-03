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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.github.phoswald.record.builder.RecordBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RecordBuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("RecordBuilderProcessor: active");
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (element instanceof TypeElement type && type.getKind() == ElementKind.RECORD) {
                    processRecord(createInfo(type));
                } else {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, //
                            "@" + annotation.getSimpleName() + " cannot be used for " + element);
                }
            }
        }
        return true;
    }

    private void processRecord(RecordInfo rec) {
        try {
            String sourceCode = createSourceCode(rec);
            String fileName = rec.packageName() + "." + rec.builderName();
            System.out.println("RecordBuilderProcessor: generating " + fileName);
            System.out.println(sourceCode);
            JavaFileObject file = processingEnv.getFiler().createSourceFile(fileName);
            try (Writer writer = file.openWriter()) {
                writer.write(sourceCode);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.toString());
        }
    }

    private static String createSourceCode(RecordInfo rec) {
        StringWriter buffer = new StringWriter();
        try (PrintWriter writer = new PrintWriter(buffer)) {
            writer.println("package " + rec.packageName() + ";");
            writer.println("public class " + rec.builderName() + " {");
            for(var comp : rec.components()) {
                writer.println("    private " + comp.type() + " " + comp.name() + ";");
            }
            writer.println("    public " + rec.builderName() + "() { }");
            writer.println("    public " + rec.builderName() + "(" + rec.className() + " inst) {");
            for(var comp : rec.components()) {
                writer.println("        this." + comp.name() + " = inst." + comp.name() + "();");
            }
            writer.println("    }");
            for(var component : rec.components()) {
                writer.println("    public " + rec.builderName() + " " + component.name() + "(" + component.type() + " " + component.name() + ") {");
                writer.println("        this." + component.name() + " = " + component.name() + ";");
                writer.println("        return this;");
                writer.println("    }");
            }
            writer.println("    public " + rec.className() + " build() {");
            writer.println("        return new " + rec.className() + "(" + String.join(", ", rec.componentNames()) + ");");
            writer.println("    }");
            writer.println("}");
        }
        return buffer.toString();
    }

    private static RecordInfo createInfo(TypeElement type) {
        return new RecordInfo( //
                type.getEnclosingElement().toString(), //
                type.getSimpleName().toString(), //
                type.getSimpleName().toString() + "Builder", //
                type.getRecordComponents().stream() //
                        .map(rc -> new RecordComponentInfo(rc.getSimpleName().toString(), rc.asType().toString())) //
                        .toList());
    }

    private static record RecordInfo( //
            String packageName, //
            String className, //
            String builderName, //
            List<RecordComponentInfo> components) {
        private List<String> componentNames() {
            return components().stream().map(comp -> comp.name()).toList();
        }
    }

    private static record RecordComponentInfo(String name, String type) { }
}
