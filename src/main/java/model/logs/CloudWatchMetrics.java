package model.logs;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CloudWatchMetrics {

  @SerializedName("CloudWatchMetrics")
  private List<CloudWatchMetric> metrics;

  @SerializedName("Timestamp")
  private long timestamp;

  public List<CloudWatchMetric> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<CloudWatchMetric> metrics) {
    this.metrics = metrics;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
