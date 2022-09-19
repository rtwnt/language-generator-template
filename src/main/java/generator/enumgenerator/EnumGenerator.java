package generator.enumgenerator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import generator.csv.WalsDataSetReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.lang.model.element.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EnumGenerator {

    private static Logger LOGGER = LogManager.getLogger();
    public static void main(String[] args) throws Exception {
        final var parameterData = WalsDataSetReader.readParameterData();
        final var parameters = parameterData.values().stream().collect(Collectors.groupingBy(parameterValue -> parameterValue.getParameter()));
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(String.class, "originalName")
                .addParameter(String.class, "parameterValueId")
                .addStatement("this.parameterValueId = parameterValueId")
                .build();

        final var externalClass = TypeSpec.classBuilder("Parameters");

        parameters.entrySet().forEach(entry -> {
            LOGGER.info("Generating class for {}", entry.getKey());
            final var builder = TypeSpec
                    .enumBuilder(prepareClassName(entry.getKey().getName()))
                    .addMethod(constructor)
                    .addField(String.class, "parameterValueId", Modifier.PRIVATE, Modifier.FINAL)
                    .addModifiers(Modifier.PUBLIC);
            entry.getValue().forEach(parameterValue -> {
                try {
                    builder.addEnumConstant(
                            prepareEnumConstantName(parameterValue.getName(), parameterValue.getId()),
                            TypeSpec.anonymousClassBuilder("$S, $S", parameterValue.getName(), parameterValue.getId()).build()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            final TypeSpec walsParameterEnum = builder.build();
            externalClass.addType(walsParameterEnum);
        });

        final var external = externalClass.build();

        JavaFile javaFile = JavaFile
                .builder("generator.enums", external)
                .indent("  ")
                .build();

        javaFile.writeTo(Path.of("src/main/java"));
    }
    private static String prepareClassName(String initalValue) {
        return Arrays.stream(initalValue.split("[\s-:']"))
                .map(EnumGenerator::toUpperCase)
                .collect(Collectors.joining())
                .replace("/", "And");
    }

    private static String prepareEnumConstantName(String initalValue, String id) throws Exception {
        try {
            Double.parseDouble(initalValue);
            return "_" + initalValue.replace(".", "_");

        } catch (NumberFormatException e) {
            // moving along
        }

        if (initalValue.matches("^\\d+.+")) {
            initalValue = "_" + initalValue;
        }

        if (id.startsWith("2A")) {
            initalValue = initalValue.replace("(", "").replace(")", "");
        }

        return initalValue.replace("VNeg (a)", "VNegA")
                .replace("VNeg (b)", "VNegB")
                .replace("(Neg)", "OptionalNegWord")
                .replace("(-Neg)", "OptionalNegSuffix")
                .replace("(Neg-)", "OptionalNegPrefix")
                .replace("-Neg", "NegSuffix")
                .replace("Neg-", "NegPreffix")
                .replace("(marked nominative)", "marked nominative")
                .replace("(standard)", "standard")
                .replace("(RelN)", "RelN")
                .replace("(NRel)", "NRel")
                .replace("(or more)", "or more")
                .replace("(= Only benefactive)", "only benefactive")
                .replace("(= no suppletive imperatives reported in the reference material)", "no suppletive imperatives reported")
                .replace("(no weight-sensitivity)", "no weight-sensitivity")
                .replace(";", "_")
                .replace("\"", "")
                .replace(".", "_")
                .replaceAll("[\\[\\],]", "")
                .replaceAll("([a-z])([A-Z]+)", "$1_$2")// camel case to snake case
                .replaceAll("[\s\\-:']", "_")
                .replace("&", "_AND_")
                .replace("+", "_PLUS_")
                .replace("/", "_OR_")
                .replace("_+$", "")
                .toUpperCase();
    }

    private static String toUpperCase(String string) {
        if (string.isBlank()) {
            return string;
        }
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

}
