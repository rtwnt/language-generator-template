package generator.statistics;

import com.google.common.collect.Sets;
import generator.data.Language;
import generator.data.Parameter;
import generator.data.ParameterValue;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProbablityCalculator {
    private final Double log2 = Math.log(2);

    @Getter
    private final Map<ParameterValue, List<Integer>> paramValueOccurenceCounts = new HashMap<>();
    private final Map<JointParamValueCooccurence, List<Integer>> jointParamValueOccurenceCounts = new HashMap<>();

    public void addLanguageData(Set<Language> languages) {
        final var languagesByParameterValues = groupLanguagesByParameterValues(languages);
        languagesByParameterValues.forEach((parameterValue, languageSet) -> {
            paramValueOccurenceCounts.computeIfAbsent(parameterValue, p -> new ArrayList<>()).add(languageSet.size());
        });
        final var parameterValues = languagesByParameterValues.keySet();
        if (parameterValues.size() < 2) {
            // there was a language group (family + macroarea) with just one parameter value being attested
            return;
        }
        final var parameterValueCooccurences = getJoinParamValueOccurences(parameterValues);
        parameterValueCooccurences.forEach(jointParamValueCooccurence -> {
            jointParamValueOccurenceCounts.computeIfAbsent(jointParamValueCooccurence, p -> new ArrayList<>())
                    .add(jointParamValueCooccurence.getCooccuringParameterValues()
                     .stream()
                     .map(languagesByParameterValues::get)
                     .reduce(Sets::intersection)
                     .orElseGet(HashSet::new)
                     .size());
        });
    }

    public CompleteProbabilityData calculateCompleteData2() {

        final var averageParamValueOccurenceCounts = paramValueOccurenceCounts.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().collect(Collectors.averagingInt(Integer::intValue))
                ));

        final var averageParamOccurenceCounts = averageParamValueOccurenceCounts.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getKey().getParameter(),
                        Collectors.summingDouble(Map.Entry::getValue)
                        )
                );
        final var marginalProbabilities = averageParamValueOccurenceCounts.keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        parameterValue -> averageParamValueOccurenceCounts.get(parameterValue) / averageParamOccurenceCounts.get(parameterValue.getParameter())
                ));

        final var averageJointParamValueOccurenceCounts = jointParamValueOccurenceCounts.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().collect(Collectors.averagingInt(Integer::intValue))
                ));

        final var averageJointParamOccurenceCounts = averageJointParamValueOccurenceCounts.entrySet()
                .stream()
                .collect(Collectors.groupingBy(
                                e -> e.getKey().getJointParamCooccurence(),
                                Collectors.summingDouble(Map.Entry::getValue)
                        )
                );

        final var jointProbabilities = averageJointParamValueOccurenceCounts.keySet()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        jointParamValueCooccurence -> averageJointParamValueOccurenceCounts.get(jointParamValueCooccurence) / averageJointParamOccurenceCounts.get(jointParamValueCooccurence.getJointParamCooccurence())
                        )
                );

        return new CompleteProbabilityData(marginalProbabilities, jointProbabilities, averageJointParamValueOccurenceCounts);
    }

    private Set<JointParamValueCooccurence> getJoinParamValueOccurences(Set<ParameterValue> parameterValues) {
        return Sets.combinations(parameterValues, 2).stream()
                .filter(s -> s.stream().map(ParameterValue::getParameter).distinct().count() > 1)
                .map(JointParamValueCooccurence::new).collect(Collectors.toSet());
    }

    private Map<ParameterValue, Set<Language>> groupLanguagesByParameterValues(Set<Language> languages) {
        return languages.stream()
                .flatMap(l -> l.getParameterValues().stream().map(v -> Pair.of(v, l)))
                .collect(
                        Collectors.groupingBy(
                                Pair::getLeft,
                                Collectors.mapping(Pair::getRight, Collectors.toSet())
                        )
                );
    }

    private Map<ParameterValue, Integer> countParameterValueOccurences(
            Map<ParameterValue, Set<Language>> languagesByParameterValues
    ) {
        return languagesByParameterValues.keySet().stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                paramValue -> languagesByParameterValues.get(paramValue).size()
                        )
                );
    }

    private Map<Parameter, Integer> countParamOccurences(
            Map<ParameterValue, Integer> paramValueOccurenceCounts
    ) {
        return paramValueOccurenceCounts.keySet().stream()
                .collect(
                        Collectors.groupingBy(
                                ParameterValue::getParameter,
                                Collectors.summingInt(paramValueOccurenceCounts::get)
                        )
                );
    }

    private <T> Map<T, Double> mapToAverageValues(Map<T, List<Integer>> map) {
        return map.entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                m -> m.getValue()
                                        .stream()
                                        .collect(
                                                Collectors.averagingInt(Integer::intValue)
                                        )
                        )
                );
    }

    private Map<JointParamCooccurence, Double> calculateMutualInformation(Map<JointParamValueCooccurence, Double> averageJointProbabilities, Map<ParameterValue, Double> averageMarginalProbabilities) {
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
