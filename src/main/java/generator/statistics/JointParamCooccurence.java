package generator.statistics;

import com.google.common.collect.Sets;
import generator.data.Parameter;
import lombok.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Set;

@Value
public class JointParamCooccurence {
    private static Logger LOGGER = LogManager.getLogger();
    Set<Parameter> cooccuringParams;

    public JointParamCooccurence(Set<Parameter> cooccuringParams) {
        this.cooccuringParams = cooccuringParams;
    }

    public static JointParamCooccurence fromParamValueList(List<Parameter> params) {
        if (params == null || params.size() != 2) {
            LOGGER.error("Expected exactly two params");
        }
        return new JointParamCooccurence(Sets.newHashSet(params));
    }
}
