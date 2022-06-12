package generator.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ChapterCsvBean extends CsvBean {
    @CsvBindByName(column = "Area_ID")
    private String areaId;
}
