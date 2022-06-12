package generator.statistics;

import com.google.common.collect.Sets;
import generator.data.Parameter;
import generator.improvedpredictionclasses.ParameterType;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ParameterPrediction {
    Parameter source;
    Parameter target;
    Double mutualInformation;
    List<ParameterValuePrediction> parameterValuePredictionList;

    public boolean belongToTheSameArea() {
        return source.getChapter()
                .getAreaId()
                .equals(
                        target.getChapter()
                                .getAreaId()
                );
    }

    public boolean parametersHaveTheSameTag() {
        final var sourceType = ParameterType.find(this.source);
        if (sourceType.isEmpty()) {
            return false;
        }

        final var targetType = ParameterType.find(this.target);
        if (targetType.isEmpty()) {
            return false;
        }

        return !Sets.intersection(sourceType.get().getTags(), targetType.get().getTags()).isEmpty();
    }

    public boolean anySourceValuePredictsLessOrEqualMarginProbabilityForAnyTargetValue(Double margin) {
        return parameterValuePredictionList.stream()
                .map(ParameterValuePrediction::getProbability)
                .anyMatch(p -> p <= margin);
    }
}
