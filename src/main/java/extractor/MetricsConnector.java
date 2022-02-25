package extractor;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.model.FilterLogEventsRequest;
import com.amazonaws.services.logs.model.FilteredLogEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import model.ProjectAnalysisMetrics;
import model.logs.CloudWatchEntry;
import model.logs.CloudWatchMetricDefinition;

public class MetricsConnector {

  private static final Logger LOGGER = Logger.getLogger(MetricsConnector.class.getName());
  private static final Gson GSON = new Gson();

  private static final String LOG_GROUP = "/SonarCloud/Autoscan/WorkerForJava/Fargate/Task/squad-1";
  private static final int START_TIME_MARGIN_HOUR = -3;
  private static final int END_TIME_MARGIN_HOUR = 3;

  private static final List<String> TARGET_METRICS = Arrays.asList(
    "JavaAnalysisFinishedCount",
    "WorkerForJavaCloneDuration",
    "WorkerForJavaTaskDuration",
    "DownloadedArtifactsPercentage",
    "ParsedArtifactsCount",
    "ConstructDependencyGraphDuration",
    "DownloadDependenciesDuration",
    "ResolveDependenciesDuration",
    "WorkerForJavaQueueLatency"
  );

  private final AWSLogs logsClient;
  private ProjectAnalysisMetrics metrics;

  public MetricsConnector(AWSLogs logsClient) {
    this.logsClient = logsClient;
  }

  public ProjectAnalysisMetrics getMetrics(String projectKey, String branch, Date analysisDate) {
    metrics = new ProjectAnalysisMetrics();
    FilterLogEventsRequest filterRequest = new FilterLogEventsRequest()
      .withStartTime(getTimeWithMargin(analysisDate, START_TIME_MARGIN_HOUR))
      .withEndTime(getTimeWithMargin(analysisDate, END_TIME_MARGIN_HOUR))
      .withFilterPattern("{($.log_type = \"METRICS\") && ($.project=\"" + projectKey + "\") && ($.branch=\"" + branch + "\")}")
      .withLogGroupName(LOG_GROUP);

    logsClient.filterLogEvents(filterRequest).getEvents().forEach(this::extractMetricsFromEvent);
    return metrics;
  }

  private long getTimeWithMargin(Date current, int amount) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(current);
    calendar.add(Calendar.HOUR, amount);
    return calendar.getTimeInMillis();
  }

  private void extractMetricsFromEvent(FilteredLogEvent event) {
    CloudWatchEntry cw = GSON.fromJson(event.getMessage(), CloudWatchEntry.class);
    cw.getCloudWatchMetrics().getMetrics().forEach(cwMetrics ->
      cwMetrics.getMetrics().stream()
        .filter(this::isTargetMetric)
        .forEach(metricDefinition -> populateMetric(event, metricDefinition))
    );
  }

  private boolean isTargetMetric(CloudWatchMetricDefinition metricDefinition) {
    return TARGET_METRICS.contains(metricDefinition.getName());
  }

  private void populateMetric(FilteredLogEvent event, CloudWatchMetricDefinition metricDefinition) {
    JsonObject json = GSON.fromJson(event.getMessage(), JsonObject.class);
    ProjectAnalysisMetrics.PopulateMetricCommand.populateValue(
      metrics, metricDefinition.getName(), json.get(metricDefinition.getName()).getAsString());
    LOGGER.info(metricDefinition.getName() + ": " + json.get(metricDefinition.getName()).getAsString());
  }

}
