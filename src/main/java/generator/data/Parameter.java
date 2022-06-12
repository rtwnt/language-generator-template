package generator.data;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Parameter implements Comparable<Parameter> {
    String id;
    String name;
    Chapter chapter;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return Objects.equal(id, parameter.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public int compareTo(Parameter o) {
        return this.id.compareTo(o.getId());
    }

    @Override
    public String toString() {
        return "generator.data.Parameter{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
