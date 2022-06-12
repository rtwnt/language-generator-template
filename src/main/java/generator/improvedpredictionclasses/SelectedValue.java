package generator.improvedpredictionclasses;

import generator.data.ParameterValue;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class SelectedValue {
    ParameterValue value;
    Set<ParameterValue> sources;

    public SelectedValue(ParameterValue value, Set<ParameterValue> sources) {
        this.value = value;
        if (sources == null) {
            sources = Collections.emptySet();
        }
        this.sources = sources;
    }

    @Override
    public String toString() {
        final var sourcesStr = sources.stream().map(s -> s.getParameter().getName() + ": " + s.getName()).collect(Collectors.joining(", "));
        return value.getParameter().getId() + " : " + value.getParameter().getName() + ": " + value.getName() + ", number of sources = " + sources.size() + ", source area ids = " + sources.stream().map(s -> s.getParameter().getChapter().getAreaId()).distinct().collect(Collectors.joining(", "));
    }
}
