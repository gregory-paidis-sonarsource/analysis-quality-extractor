package model;

import java.util.Map;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class ProjectAnalysisMetrics {

  private int JavaAnalysisFinishedCount;
  private int WorkerForJavaCloneDuration;
  private int WorkerForJavaTaskDuration;
  private float DownloadedArtifactsPercentage;
  private int ParsedArtifactsCount;
  private int ConstructDependencyGraphDuration;
  private int DownloadDependenciesDuration;
  private int ResolveDependenciesDuration;
  private int WorkerForJavaQueueLatency;

  public int getJavaAnalysisFinishedCount() {
    return JavaAnalysisFinishedCount;
  }

  public void setJavaAnalysisFinishedCount(int javaAnalysisFinishedCount) {
    JavaAnalysisFinishedCount = javaAnalysisFinishedCount;
  }

  public int getWorkerForJavaCloneDuration() {
    return WorkerForJavaCloneDuration;
  }

  public void setWorkerForJavaCloneDuration(int workerForJavaCloneDuration) {
    WorkerForJavaCloneDuration = workerForJavaCloneDuration;
  }

  public int getWorkerForJavaTaskDuration() {
    return WorkerForJavaTaskDuration;
  }

  public void setWorkerForJavaTaskDuration(int workerForJavaTaskDuration) {
    WorkerForJavaTaskDuration = workerForJavaTaskDuration;
  }

  public float getDownloadedArtifactsPercentage() {
    return DownloadedArtifactsPercentage;
  }

  public void setDownloadedArtifactsPercentage(float downloadedArtifactsPercentage) {
    DownloadedArtifactsPercentage = downloadedArtifactsPercentage;
  }

  public int getParsedArtifactsCount() {
    return ParsedArtifactsCount;
  }

  public void setParsedArtifactsCount(int parsedArtifactsCount) {
    ParsedArtifactsCount = parsedArtifactsCount;
  }

  public int getConstructDependencyGraphDuration() {
    return ConstructDependencyGraphDuration;
  }

  public void setConstructDependencyGraphDuration(int constructDependencyGraphDuration) {
    ConstructDependencyGraphDuration = constructDependencyGraphDuration;
  }

  public int getDownloadDependenciesDuration() {
    return DownloadDependenciesDuration;
  }

  public void setDownloadDependenciesDuration(int downloadDependenciesDuration) {
    DownloadDependenciesDuration = downloadDependenciesDuration;
  }

  public int getResolveDependenciesDuration() {
    return ResolveDependenciesDuration;
  }

  public void setResolveDependenciesDuration(int resolveDependenciesDuration) {
    ResolveDependenciesDuration = resolveDependenciesDuration;
  }

  public int getWorkerForJavaQueueLatency() {
    return WorkerForJavaQueueLatency;
  }

  public void setWorkerForJavaQueueLatency(int workerForJavaQueueLatency) {
    WorkerForJavaQueueLatency = workerForJavaQueueLatency;
  }

  private interface PopulateCommand {
    void populate(ProjectAnalysisMetrics metrics, String value);
  }

  public static class PopulateMetricCommand {

    private PopulateMetricCommand() {
      throw new IllegalStateException("Utility class");
    }

    private static Map<String, PopulateCommand> populateCommandProvider() {
      return Map.of(
        "JavaAnalysisFinishedCount",
        (metrics, value) -> metrics.setJavaAnalysisFinishedCount(parseInt(value)),
        "WorkerForJavaCloneDuration",
        (metrics, value) -> metrics.setWorkerForJavaCloneDuration(parseInt(value)),
        "WorkerForJavaTaskDuration",
        (metrics, value) -> metrics.setWorkerForJavaTaskDuration(parseInt(value)),
        "DownloadedArtifactsPercentage",
        (metrics, value) -> metrics.setDownloadedArtifactsPercentage(parseFloat(value)),
        "ParsedArtifactsCount",
        (metrics, value) -> metrics.setParsedArtifactsCount(parseInt(value)),
        "ConstructDependencyGraphDuration",
        (metrics, value) -> metrics.setConstructDependencyGraphDuration(parseInt(value)),
        "DownloadDependenciesDuration",
        (metrics, value) -> metrics.setDownloadDependenciesDuration(parseInt(value)),
        "ResolveDependenciesDuration",
        (metrics, value) -> metrics.setResolveDependenciesDuration(parseInt(value)),
        "WorkerForJavaQueueLatency",
        (metrics, value) -> metrics.setWorkerForJavaQueueLatency(parseInt(value))
      );
    }

    public static void populateValue(ProjectAnalysisMetrics metrics, String metric, String value) {
      PopulateCommand command = populateCommandProvider().get(metric);
      command.populate(metrics, value);
    }

  }


}
