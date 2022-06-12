package generator.statistics;

import generator.data.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Data
@AllArgsConstructor
public class ConditionalParamCooccurence {
    private static Logger LOGGER = LogManager.getLogger();
    Parameter preexisting;
    Parameter nextSelected;

    public static ConditionalParamCooccurence fromParamList(List<Parameter> params) {
        if (params == null || params.size() != 2) {
            LOGGER.error("Expected exactly two params");
        }
        return new ConditionalParamCooccurence(params.get(0), params.get(1));
    }

    @Override
    public String toString() {
        return "generator.statistics.ConditionalParamCooccurence{" +
                "preexisting=" + preexisting.getName() +
                ", nextSelected=" + nextSelected.getName() +
                '}';
    }
}
