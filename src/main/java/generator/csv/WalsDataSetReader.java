package generator.csv;

import com.google.common.collect.Sets;
import com.opencsv.bean.CsvToBeanBuilder;
import generator.data.Chapter;
import generator.data.Language;
import generator.data.Parameter;
import generator.data.ParameterValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WalsDataSetReader {
    public record FamilyAndMacroarea(String family, String macroarea) {}

    private static final String WRITING_SYSTEMS_ID = "141A";
    private static final String Irregular_Negatives_in_Sign_Languages = "139A";
    private static final String Question_Particles_in_Sign_Languages = "140A";
    private static final String TEA = "138A";
    private static final String NASAL_VOWELS_IN_WEST_AFRICA = "10B";
    private static final String[] excludedParamIds = new String[] {
            WRITING_SYSTEMS_ID,
            Irregular_Negatives_in_Sign_Languages,
            Question_Particles_in_Sign_Languages,
            TEA,
            NASAL_VOWELS_IN_WEST_AFRICA
    };
    private static final Logger LOGGER = LogManager.getLogger();

    public static Map<String, ParameterValue> readParameterData() throws Exception {
        final var chapters = readValues("chapters", ChapterCsvBean.class).stream()
                .map(c -> new Chapter(c.getId(), c.getAreaId()))
                .collect(Collectors.toMap(Chapter::getId, Function.identity()));

        final var parameters = readValues("parameters", ParameterCsvBean.class).stream()
                .filter(c -> !Sets.newHashSet(excludedParamIds).contains(c.getId()))
                .map(p -> new Parameter(p.getId(), p.getName(), chapters.get(p.getChapterId())))
                .collect(Collectors.toMap(Parameter::getId, Function.identity()));

        final var parameterValues = readValues("codes", CodeCsvBean.class)
                .stream()
                .filter(c -> !Sets.newHashSet(excludedParamIds).contains(c.getParameterId()))
                .map(c -> new ParameterValue(c.getId(), c.getName(), parameters.get(c.getParameterId())))
                .collect(Collectors.toMap(ParameterValue::getId, Function.identity()));

        return parameterValues;
    }

    public static Map<FamilyAndMacroarea, List<Language>> readLanguageDataWithAllRelations() throws Exception {
        final var parameterValues = readParameterData();

        final var languages = readValues("languages", LanguageCsvBean.class).stream()
                .map(l -> new Language(l.getId(), l.getName(), new HashSet<>(), l.getMacroarea(), l.getFamily()))
                .collect(Collectors.toMap(Language::getId, Function.identity()));

        readValues("values", ValueCsvBean.class).stream()
                .filter(c -> !Sets.newHashSet(excludedParamIds).contains(c.getParameterId()))
                .forEach( value -> languages.get(value.getLanguageId()).addParameterValue(parameterValues.get(value.getCodeId()))
                );

        return languages.values().stream().collect(Collectors.groupingBy(l -> new FamilyAndMacroarea(l.getFamily(), l.getMacroArea())));
    }

    private static <T extends CsvBean> List<T> readValues(String name, Class<T> clazz) throws Exception {
        LOGGER.info("Starting to read {} from csv file...", name);
        var result = new CsvToBeanBuilder<T>(new FileReader(getPath(name)))
                .withType(clazz).build().parse();
        LOGGER.info("Done");
        return result;
    }

    private static String getPath(String name) {
        return "src/main/resources/wals/cldf/" + name + ".csv";
    }
}
