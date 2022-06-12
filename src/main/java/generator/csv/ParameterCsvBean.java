package generator.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ParameterCsvBean extends CsvBean {
    @CsvBindByName(column = "Name")
    private String name;

    @CsvBindByName(column = "Chapter_ID")
    private String chapterId;
}
