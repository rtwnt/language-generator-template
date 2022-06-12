package generator.improvedpredictionclasses;

import generator.data.ParameterValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thejavaguy.prng.generators.PRNG;
import org.thejavaguy.prng.generators.util.IntRange;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProbabilityData {
    private static final Logger LOGGER = LogManager.getLogger();
    Map<Double, List<ParameterValue>> weightsToParamValueSets;
    Set<ParameterValue> sources;

    public ProbabilityData(Map<ParameterValue, Double> probabilites, Set<ParameterValue> sources) {
        this.weightsToParamValueSets = groupByWeights(probabilites);
        if (sources == null) {
            sources = Collections.emptySet();
        }
        this.sources = sources;
    }

    public Set<ParameterValue> getAllParameterValuesWithNonZeroProbability() {
        return weightsToParamValueSets.entrySet()
                .stream()
                .filter(e -> e.getKey() > 0.0)
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toSet());
    }

    private Map<Double, List<ParameterValue>> groupByWeights(Map<ParameterValue, Double> probabilites) {
        return probabilites.entrySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                Map.Entry::getValue,
                                Collectors.mapping(
                                        Map.Entry::getKey,
                                        Collectors.toList()
                                )
                        )
                );
    }

    public Optional<SelectedValue> getRandomParameterValueUsingLinearScan(PRNG.Smart generator) {
        final var sumOfWeights = weightsToParamValueSets.keySet().stream().mapToDouble(p -> p).sum();
        LOGGER.info("SUM OF WEIGHTS: {}", sumOfWeights);
        var remainingDistance = generator.nextDouble() * sumOfWeights;
        LOGGER.info("REMAINING DISTANCE: {}", remainingDistance);
        for (var weight : weightsToParamValueSets.keySet().stream().sorted(Comparator.reverseOrder()).toList()) {
            remainingDistance -= weight;
            if (remainingDistance < 0) {
                var values = weightsToParamValueSets.get(weight);
                var index = values.size() == 1? 0 : generator.nextInt(new IntRange(0, values.size() - 1));
                return Optional.of(new SelectedValue(values.get(index), sources));
            }
            LOGGER.info("Attempting to pick the next value");
        }

        LOGGER.warn("Couldn't select a value. Max prediction weight = {}", weightsToParamValueSets.keySet().stream().max(Comparator.comparing(Function.identity())));

        return Optional.empty();
    }

    @Override
    public String toString() {
        return "ProbabilityData{" +
                "weightsToParamValueSets=" + weightsToParamValueSets +
                '}';
    }
}
