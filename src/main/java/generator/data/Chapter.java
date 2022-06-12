package generator.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Chapter implements Comparable<Chapter> {

    String id;
    String areaId;

    @Override
    public int compareTo(Chapter o) {
        return 0;
    }
}
