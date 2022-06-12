package generator.statistics;

import com.google.common.collect.Sets;
import generator.data.ParameterValue;
import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
public class JointParamValueCooccurence {
    private static Logger LOGGER = LogManager.getLogger();
    Set<ParameterValue> cooccuringParameterValues;

    public JointParamValueCooccurence(Set<ParameterValue> cooccuringParameterValues) {
        this.cooccuringParameterValues = cooccuringParameterValues;
    }

    public static JointParamValueCooccurence fromParamValueList(List<ParameterValue> paramValues) {
        if (paramValues == null || paramValues.size() != 2) {
            LOGGER.error("Expected exactly two params");
        }
        return new JointParamValueCooccurence(Sets.newHashSet(paramValues));
    }

    public JointParamCooccurence getJointParamCooccurence() {
        return new JointParamCooccurence(getCooccuringParameterValues().stream()
                .map(ParameterValue::getParameter)
                .collect(Collectors.toSet()));
    }
}
