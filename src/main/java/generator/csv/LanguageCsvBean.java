package generator.csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class LanguageCsvBean extends CsvBean {
    @CsvBindByName(column = "Name")
    private String name;
    @CsvBindByName(column = "Macroarea")
    private String macroarea;
    @CsvBindByName(column = "Family")
    private String family;
}
