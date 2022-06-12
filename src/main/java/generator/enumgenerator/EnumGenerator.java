package generator.enumgenerator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;

public class EnumGenerator {
    public static void main(String[] args) throws Exception {
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(String.class, "parameterId")
                .addStatement("this.parameterId = parameterId")
                .build();

        final var externalClass = TypeSpec.classBuilder("Parameters");

        TypeSpec walsParameterEnum = TypeSpec
                .enumBuilder("FirstParam")
                .addMethod(constructor)
                .addField(String.class, "parameterId", Modifier.PRIVATE, Modifier.FINAL)
                .addModifiers(Modifier.PUBLIC)
                .addEnumConstant("FIRST", TypeSpec.anonymousClassBuilder("$S", "first").build())
                .addEnumConstant("SECOND", TypeSpec.anonymousClassBuilder("$S", "second").build())
                .addEnumConstant("THIRD", TypeSpec.anonymousClassBuilder("$S", "third").build())
                .build();

        externalClass.addType(walsParameterEnum);
        final var external = externalClass.build();

        JavaFile javaFile = JavaFile
                .builder("generator.enums", external)
                .indent("  ")
                .build();

        javaFile.writeTo(Path.of("src/main/java"));
    }
}
