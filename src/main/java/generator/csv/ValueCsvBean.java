package generator.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ValueCsvBean extends CsvBean {
    @CsvBindByName(column = "Language_ID")
    private String languageId;

    @CsvBindByName(column = "Parameter_ID")
    private String parameterId;

    @CsvBindByName(column = "Code_ID")
    private String codeId;
}
