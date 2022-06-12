package generator.data;

import com.google.common.base.Objects;
import lombok.Data;

@Data
public class ParameterValue implements Comparable<ParameterValue> {
    String id;
    String name;
    Parameter parameter;

    public ParameterValue(String id, String name, Parameter parameter) {
        this.id = id;
        this.name = name;
        this.parameter = parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterValue that = (ParameterValue) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "generator.data.ParameterValue{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(ParameterValue o) {
        return id.compareTo(o.id);
    }
}
