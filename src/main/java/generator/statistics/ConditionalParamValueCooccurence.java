package generator.statistics;

import generator.data.ParameterValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Data
@AllArgsConstructor
public class ConditionalParamValueCooccurence {
    private static Logger LOGGER = LogManager.getLogger();
    ParameterValue preexisting;
    ParameterValue nextSelected;

    public static ConditionalParamValueCooccurence fromParamValueList(List<ParameterValue> paramValues) {
        if (paramValues == null || paramValues.size() != 2) {
            LOGGER.error("Expected exactly two params");
        }
        return new ConditionalParamValueCooccurence(paramValues.get(0), paramValues.get(1));
    }

    @Override
    public String toString() {
        return "generator.statistics.ConditionalParamCooccurence{" +
                "preexisting=" + preexisting.getParameter().getName() + ": " + preexisting.getName() +
                ", nextSelected=" + nextSelected.getParameter().getName() + ": " + nextSelected.getName() +
                '}';
    }
}
