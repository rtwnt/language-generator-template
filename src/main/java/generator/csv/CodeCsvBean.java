package generator.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class CodeCsvBean extends CsvBean {
    @CsvBindByName(column = "Parameter_ID")
    private String parameterId;

    @CsvBindByName(column = "Name")
    private String name;

    @CsvBindByName(column = "Description")
    private String description;
}
