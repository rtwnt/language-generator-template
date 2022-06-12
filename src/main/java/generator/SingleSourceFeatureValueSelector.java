package generator;

import com.google.common.collect.Sets;
import generator.data.Parameter;
import generator.data.ParameterValue;
import generator.improvedpredictionclasses.ParameterValueType;
import generator.improvedpredictionclasses.ProbabilityData;
import generator.improvedpredictionclasses.SelectedValue;
import generator.statistics.CompleteProbabilityData;
import generator.statistics.ConditionalParamCooccurence;
import generator.statistics.ParameterPrediction;
import generator.statistics.ParameterValuePrediction;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thejavaguy.prng.generators.PRNG;
import org.thejavaguy.prng.generators.R250;
import org.thejavaguy.prng.generators.XorshiftPlus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SingleSourceFeatureValueSelector {

    private final List<Parameter> parameters;
    private static final Logger LOGGER = LogManager.getLogger();
    private final CompleteProbabilityData probabilityData;
    private final Map<Parameter, Set<ParameterValue>> parameterToAvailableValues;

    @SneakyThrows
    public SingleSourceFeatureValueSelector(
            Set<ParameterValue> parameterValues,
            CompleteProbabilityData probabilityData
    ) {
        this.parameters = parameterValues.stream()
                .map(ParameterValue::getParameter)
                .distinct()
                .sorted(getSortingComparator())
                .toList();
        this.parameterToAvailableValues = parameterValues.stream()
                .collect(Collectors.groupingBy(ParameterValue::getParameter, Collectors.toSet()));
        this.probabilityData = probabilityData;
    }

    private Comparator<Parameter> getSortingComparator() {
        return Comparator.comparing((Parameter p) -> p.getId().charAt(p.getId().length() - 1)).thenComparing(p -> Integer.valueOf(p.getId().substring(0, p.getId().length() - 1)));
    }

    private Comparator<Map.Entry<Parameter, SelectedValue>> getSortingComparatorForParameterValues() {
        return Comparator.comparing((Map.Entry<Parameter, SelectedValue> p) -> p.getKey().getId().charAt(p.getKey().getId().length() - 1)).thenComparing(p -> Integer.valueOf(p.getKey().getId().substring(0, p.getKey().getId().length() - 1)));
    }

    public void generateRandomFeatureSet() {
        var generatedLanguageParamValues = new HashMap<Parameter, SelectedValue>();
        final var excludedParamValues = new HashMap<Parameter, Set<ParameterValue>>();
        final var seed = new R250().nextInt();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("SEED: " + seed);

        parameters.forEach(toSet -> {
            if (generatedLanguageParamValues.containsKey(toSet)) {
                return;
            }
            var probabilityData = getProbabilitiesBasedOnAlreadySelectedValues(generatedLanguageParamValues, toSet);
            final PRNG.Smart generator = new XorshiftPlus.Smart(new XorshiftPlus((seed + toSet.getId()).hashCode()));
            var paramValue = probabilityData.getRandomParameterValueUsingLinearScan(generator);
            paramValue.ifPresent(pv -> {
                final var type = ParameterValueType.find(pv.getValue());
                generatedLanguageParamValues.put(pv.getValue().getParameter(), pv);
            });
        });
        System.out.println("Result");
        System.out.println("Selected values: " + generatedLanguageParamValues.size());
        parameters.forEach(parameter -> System.out.println(generatedLanguageParamValues.get(parameter)));
        try {
            Files.write(Path.of("generated_values_" + System.currentTimeMillis() + "_.yaml"), generatedLanguageParamValues.entrySet()
                    .stream()
                    .sorted(getSortingComparatorForParameterValues())
                    .map(p -> p.getKey().getId() + " - " + p.getKey().getName() + ": " + Optional.of(p.getValue()).map(v -> v.getValue().getName()).orElse(StringUtils.EMPTY))
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The End");
    }

    private ProbabilityData getProbabilitiesBasedOnAlreadySelectedValues(
            HashMap<Parameter, SelectedValue> generatedLanguageParamValues, Parameter toSet
    ) {
        final var predictions = getPredictionsWithFiltering(generatedLanguageParamValues, toSet);
        Map<ParameterValue, Double> probabilities;
        if (predictions.isEmpty()) {
            probabilities = getMarginalProbabilities(toSet);
        } else {
            probabilities = getMultipliedProbabilities(predictions);
        }
        Map<ParameterValue, Double> finalProbabilities = probabilities;
        probabilities = getAlteredProbabilities(finalProbabilities);

        return new ProbabilityData(probabilities, predictions.stream().map(ParameterValuePrediction::getSource).collect(Collectors.toSet()));
    }

    private List<ParameterValuePrediction> getPredictionsWithFiltering(Map<Parameter, SelectedValue> alreadySet, Parameter toSet) {
        final var alreadySetValues = alreadySet.values().stream().map(SelectedValue::getValue).collect(Collectors.toSet());
        final var parameterPredictions = Sets.cartesianProduct(alreadySet.keySet(), Set.of(toSet)).stream()
                .map(ConditionalParamCooccurence::fromParamList)
                .map(probabilityData.getPredictions()::get).collect(Collectors.toSet());
        final var top = getTopPredictionsByMutualInformation(parameterPredictions, 5);
        final var excludingSomeValues = getAllPredictionsProvidingProbabilityLessOrEqualToMarginForAValue(parameterPredictions, 0.0);

        return Sets.union(top, excludingSomeValues).stream()
                .flatMap(paramPrediction -> paramPrediction.getParameterValuePredictionList()
                        .stream()
                        .filter(p -> alreadySetValues.contains(p.getSource()))
                )
                .collect(Collectors.toList());
    }

    private Set<ParameterPrediction> getTopPredictionsByMutualInformation(Collection<ParameterPrediction> parameterPredictions, int limit) {
        return parameterPredictions.stream()
                .filter(p -> p != null && (p.belongToTheSameArea() || p.parametersHaveTheSameTag()))
                .sorted(Comparator.comparing(ParameterPrediction::getMutualInformation).reversed())
                .limit(limit).collect(Collectors.toSet());
    }

    private Set<ParameterPrediction> getAllPredictionsProvidingProbabilityLessOrEqualToMarginForAValue(Collection<ParameterPrediction> parameterPredictions, double margin) {
        return parameterPredictions.stream()
                .filter(p -> p != null && (p.belongToTheSameArea() || p.parametersHaveTheSameTag()) && p.anySourceValuePredictsLessOrEqualMarginProbabilityForAnyTargetValue(0.0))
                .collect(Collectors.toSet());
    }

    private List<ParameterPrediction> getPredictions(
            Set<Parameter> alreadySetParameters,
            Parameter toSet) {
        return Sets.cartesianProduct(alreadySetParameters, Set.of(toSet)).stream()
                .map(ConditionalParamCooccurence::fromParamList)
                .map(probabilityData.getPredictions()::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<ParameterValuePrediction> getFilteredPredictions(List<ParameterPrediction> parameterPredictions, Set<ParameterValue> alreadySelectedValues) {
        return parameterPredictions.stream()
                .filter(p -> (p.belongToTheSameArea() || p.parametersHaveTheSameTag()))
                .sorted(Comparator.comparing(ParameterPrediction::getMutualInformation).reversed())
                .limit(5)
                .flatMap(paramPrediction -> paramPrediction.getParameterValuePredictionList().stream().filter(p -> alreadySelectedValues.contains(p.getSource())))
                .collect(Collectors.toList());
    }

    private Map<ParameterValue, Double> getAlteredProbabilities(Map<ParameterValue, Double> probabilities) {
        return probabilities.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == 0.0 ? 0.0 : 1.0));
    }

    private Map<ParameterValue, Double> getMultipliedProbabilities(List<ParameterValuePrediction> parameterValuePredictions) {
        return parameterValuePredictions.stream()
                .collect(
                        Collectors.groupingBy(
                                ParameterValuePrediction::getTarget,
                                Collectors.reducing(1.0, ParameterValuePrediction::getProbability, (a, b) -> a * b)
                        )
                );
    }

    private Map<ParameterValue, Double> getMarginalProbabilities(Parameter toSet) {
        return parameterToAvailableValues.get(toSet)
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                paramValue -> probabilityData.getMarginalProbabilities().get(paramValue)
                        )
                );
    }
}
