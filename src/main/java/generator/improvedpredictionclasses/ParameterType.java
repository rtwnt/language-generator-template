package generator.improvedpredictionclasses;

import com.google.common.collect.Sets;
import generator.data.Parameter;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static generator.improvedpredictionclasses.ParameterTag.*;

public enum ParameterType {
    CASE_SYNCRETISM("28A", CASE_INFLECTION),
    EXPONENCE_OF_SELECTED_INFLECTIONAL_FORMATIVES("21A", CASE_INFLECTION),
    NUMBER_OF_CASES("49A", CASE_INFLECTION),
    ASSYMETRICAL_CASE_MARKING("50A", CASE_INFLECTION),
    POSITION_OF_CASE_AFFIXES("51A", CASE_INFLECTION),
    ALIGNMENT_OF_CASE_MARKING_ON_FULL_NOUN_PHRASES("98A", CASE_INFLECTION),
    ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS("99A", CASE_INFLECTION),
    ;
    private final String id;
    @Getter
    private final Set<ParameterTag> tags;

    ParameterType(String id, ParameterTag... tags) {
        this.id = id;
        this.tags = Sets.newHashSet(tags);
    }

    public static Optional<ParameterType> find(Parameter parameter) {
        return Arrays.stream(ParameterType.values()).filter(parameterType -> parameter.getId().equals(parameterType.id)).findFirst();
    }
}
