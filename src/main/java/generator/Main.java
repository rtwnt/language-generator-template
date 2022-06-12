package generator;

import com.google.common.collect.Sets;
import generator.csv.WalsDataSetReader;
import generator.data.ParameterValue;
import generator.statistics.ProbablityCalculator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();
    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting...");

        var languages = WalsDataSetReader.readLanguageDataWithAllRelations();
        final var probabilityDataBuilder = new ProbablityCalculator();
        languages.forEach((key, value) -> {
            LOGGER.info("Adding data for {}", key);
            probabilityDataBuilder.addLanguageData(new HashSet<>(value));
        });
        final var probabilityData = probabilityDataBuilder.calculateCompleteData2();
        final var paramValuesById = probabilityData.getMarginalProbabilities()
                .keySet()
                .stream()
                .collect(Collectors.toMap(ParameterValue::getId, Function.identity()));

        final var selector = new SingleSourceFeatureValueSelector(Sets.newHashSet(paramValuesById.values()), probabilityData);

        selector.generateRandomFeatureSet();
    }
}
