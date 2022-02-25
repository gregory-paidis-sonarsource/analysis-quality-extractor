package model.logs;

import com.google.gson.annotations.SerializedName;

public class CloudWatchMetricDefinition {

  @SerializedName("Name")
  private String name;
  @SerializedName("Unit")
  private String unit;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }
}
