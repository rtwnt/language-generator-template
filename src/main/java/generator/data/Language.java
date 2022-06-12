package generator.data;

import com.google.common.base.Objects;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Language {
    String id;
    String name;
    Set<ParameterValue> parameterValues = new HashSet<>();
    String macroArea;
    String family;


    public Language(String id, String name, Set<ParameterValue> parameterValues, String macroArea, String family) {
        this.id = id;
        this.name = name;
        this.macroArea = macroArea;
        this.family = family;
        this.parameterValues = parameterValues;
    }

    public void addParameterValue(ParameterValue value) {
        this.parameterValues.add(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Language language = (Language) o;
        return Objects.equal(id, language.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "generator.data.Language{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
