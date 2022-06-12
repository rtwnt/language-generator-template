package generator.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class CsvBean {
    @CsvBindByName(column = "ID")
    private String id;

    public String getId() {
        return id;
    }
}
