package model.logs;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CloudWatchMetric {

  @SerializedName("Namespace")
  private String namespace;

  @SerializedName("Metrics")
  private List<CloudWatchMetricDefinition> metrics;

  @SerializedName("Dimensions")
  private List<List<String>> dimensions;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public List<List<String>> getDimensions() {
    return dimensions;
  }

  public void setDimensions(List<List<String>> dimensions) {
    this.dimensions = dimensions;
  }

  public List<CloudWatchMetricDefinition> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<CloudWatchMetricDefinition> metrics) {
    this.metrics = metrics;
  }
}
