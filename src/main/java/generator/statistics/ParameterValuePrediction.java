package generator.statistics;

import generator.data.ParameterValue;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ParameterValuePrediction {
    ParameterValue source;
    ParameterValue target;
    Double probability;
    Double observationCount;
}
