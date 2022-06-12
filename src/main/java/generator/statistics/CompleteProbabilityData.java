package generator.statistics;

import com.google.common.collect.Sets;
import generator.data.ParameterValue;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompleteProbabilityData {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Double log2 = Math.log(2);

    @Getter
    private final Map<ParameterValue, Double> marginalProbabilities;
    @Getter
    private final Map<ConditionalParamCooccurence, ParameterPrediction> predictions;

    public CompleteProbabilityData(
            Map<ParameterValue, Double> marginalProbabilities,
            Map<JointParamValueCooccurence, Double> jointProbabilities,
            Map<JointParamValueCooccurence, Double> averageJointParamValueOccurenceCounts
    ) {
        this.marginalProbabilities = marginalProbabilities;
        final var mutualInformation = calculateMutualInformation(jointProbabilities, marginalProbabilities);
        this.predictions = calculateConditionalProbabilityPredictions(
                marginalProbabilities, jointProbabilities,
                mutualInformation, averageJointParamValueOccurenceCounts);
    }

    private Map<ConditionalParamCooccurence, ParameterPrediction> calculateConditionalProbabilityPredictions(
            Map<ParameterValue, Double> averageMarginalProbabilities,
            Map<JointParamValueCooccurence, Double> averageJointProbabilities,
            Map<JointParamCooccurence, Double> mutualInformation,
            Map<JointParamValueCooccurence, Double> averageJointParamValueOccurenceCounts
    ) {
        final var parameterValuePredictions = Sets.cartesianProduct(averageMarginalProbabilities.keySet(), averageMarginalProbabilities.keySet())
                .stream()
                .filter(c -> c.stream().map(ParameterValue::getParameter).distinct().count() == 2)
                .map(ConditionalParamValueCooccurence::fromParamValueList)
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                c -> {
                                    final var cooccurence = new JointParamValueCooccurence(Set.of(c.getPreexisting(), c.getNextSelected()));
                                    return ParameterValuePrediction.builder()
                                            .source(c.getPreexisting())
                                            .target(c.getNextSelected())
                                            .probability(
                                                    averageJointProbabilities.getOrDefault(cooccurence, 0.0) /
                                                            averageMarginalProbabilities.get(c.getPreexisting())
                                            ).observationCount(averageJointParamValueOccurenceCounts.getOrDefault(cooccurence, 0.0))
                                            .build();
                                }
                        )
                );

        return parameterValuePredictions.values().stream().collect(Collectors.groupingBy(
                p -> new ConditionalParamCooccurence(p.getSource().getParameter(), p.getTarget().getParameter()))
        ).entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> ParameterPrediction.builder()
                                        .parameterValuePredictionList(e.getValue())
                                        .source(e.getKey().getPreexisting())
                                        .target(e.getKey().getNextSelected())
                                        .mutualInformation(
                                                mutualInformation.getOrDefault(
                                                        JointParamCooccurence.fromParamValueList(
                                                                List.of(
                                                                        e.getKey().getPreexisting(),
                                                                        e.getKey().getNextSelected())
                                                        ),
                                                        0.0
                                                )
                                        ).build()
                        )
                );
    }

    private Map<JointParamCooccurence, Double> calculateMutualInformation(
            Map<JointParamValueCooccurence, Double> averageJointProbabilities,
            Map<ParameterValue, Double> averageMarginalProbabilities
    ) {
        return averageJointProbabilities.keySet()
                .stream()
                .distinct()
                .map(jointParamValueCooccurence -> calculateMutualInformationComponent(jointParamValueCooccurence, averageJointProbabilities, averageMarginalProbabilities))
                .collect(
                        Collectors.groupingBy(
                                Pair::getLeft,
                                Collectors.summingDouble(Pair::getRight)
                        )
                );
    }

    private Pair<JointParamCooccurence, Double> calculateMutualInformationComponent(JointParamValueCooccurence cooccuringParameterValues, Map<JointParamValueCooccurence, Double> averageJointProbabilities, Map<ParameterValue, Double> averageMarginalProbabilities) {
        var jointProbability = averageJointProbabilities.get(cooccuringParameterValues);
        final var jointParamCooccurence = cooccuringParameterValues.getJointParamCooccurence();
        if (jointProbability == 0.0) {
            return Pair.of(jointParamCooccurence, 0.0);
        }
        var independentJointProbability = cooccuringParameterValues.getCooccuringParameterValues()
                .stream()
                .map(averageMarginalProbabilities::get)
                .reduce(1.0, (a, b) -> a * b);
        if (independentJointProbability == 0.0) {
            return Pair.of(jointParamCooccurence, 0.0);
        }
        var component =  jointProbability * Math.log(jointProbability / independentJointProbability) / log2;
        return Pair.of(jointParamCooccurence, component);
    }
}
