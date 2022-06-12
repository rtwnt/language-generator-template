package generator.improvedpredictionclasses;

import generator.data.Parameter;
import generator.data.ParameterValue;

import java.util.*;
import java.util.stream.Collectors;

import static generator.improvedpredictionclasses.ParameterType.*;

public enum ParameterValueType {
    // this also corresponds to languages having 2 cases, as per WALS article
    // this is because syncretism of such two cases would always be expressed as a lack of inflection,
    // and syncretic case forms bust be inflected forms
    CASE_SYNCRETISM_NO_CASE_MARKING("28A-1", CASE_SYNCRETISM),
    EXPONENCE_OF_SELECTED_INFLECTIONAL_FORMATIVES_NO_CASE("21A-5", EXPONENCE_OF_SELECTED_INFLECTIONAL_FORMATIVES),
    NUMBER_OF_CASES_NO_MORPHOLOGICAL_CASE_MARKING("49A-1", NUMBER_OF_CASES),// this only refers to "generally applicable case" - pronominal case is different
    ASSYMETRICAL_CASE_MARKING_NO_CASE_MARKING("50A-1", ASSYMETRICAL_CASE_MARKING),// Other values may combine with generally 0 cases
    POSITION_OF_CASE_AFFIXES_NO_CASE_AFFIXES_OR_ADPOSITIONAL_CLITICS("51A-9", POSITION_OF_CASE_AFFIXES),
    ALIGNMENT_OF_CASE_MARKING_ON_FULL_NOUN_PHRASES_NEUTRAL("98A-1", ALIGNMENT_OF_CASE_MARKING_ON_FULL_NOUN_PHRASES),
    ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS_NEUTRAL("99A-1", ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS),// can be paired with 0 marking
    ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS_NONE("99A-7", ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS), // can be paired with 0 generally applicable cases
    ;
    private final String id;
    private final ParameterType parameterType;

    ParameterValueType(String id, ParameterType paramType) {
        this.id = id;
        this.parameterType = paramType;
    }

    public Map<Parameter, Set<ParameterValue>> getParameterValueAvailabilityModifictaion(Collection<ParameterValue> allParameters) {
        final var implyingLackOfCases = List.of(
                CASE_SYNCRETISM_NO_CASE_MARKING,
                EXPONENCE_OF_SELECTED_INFLECTIONAL_FORMATIVES_NO_CASE,
                NUMBER_OF_CASES_NO_MORPHOLOGICAL_CASE_MARKING,
                ASSYMETRICAL_CASE_MARKING_NO_CASE_MARKING,
                POSITION_OF_CASE_AFFIXES_NO_CASE_AFFIXES_OR_ADPOSITIONAL_CLITICS
        );

        final var impliedValueTypes = new ArrayList<>(implyingLackOfCases);
        impliedValueTypes.add(ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS_NEUTRAL);
        impliedValueTypes.add(ALIGNMENT_OF_CASE_MARKING_ON_PRONOUNS_NONE);

        final var impliedValueIds = impliedValueTypes.stream().map(p -> p.id).collect(Collectors.toSet());

        if (implyingLackOfCases.contains(this)) {
            final var other = allParameters
                    .stream()
                    .filter(pv -> !pv.getId().equals(this.id) && impliedValueIds.contains(pv.getId()))
                    .collect(Collectors.groupingBy(ParameterValue::getParameter, Collectors.toSet()));

            return other;
        }

        return Collections.emptyMap();
    }

    public static Optional<ParameterValueType> find(ParameterValue value) {
        return Arrays.stream(ParameterValueType.values()).filter(e -> e.id.equals(value.getId())).findFirst();
    }
}
