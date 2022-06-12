package generator.enums;

import java.lang.String;

class Parameters {
  public enum FirstParam {
    FIRST("first"),

    SECOND("second"),

    THIRD("third");

    private final String parameterId;

    FirstParam(String parameterId) {
      this.parameterId = parameterId;
    }
  }
}
