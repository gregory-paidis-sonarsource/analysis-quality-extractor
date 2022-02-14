package task;

import extractor.ApiConnector;
import extractor.ProjectAnalysis;
import java.util.List;
import java.util.stream.Collectors;
import model.Component;
import model.ProjectAnalysisQuality;

public class AnalysisQualityGenerator {

  private static final String SC_STAGING_URL = "https://sc-staging.io/";
  private static final String ORG_AUTOSCAN_FOR_JAVA_CI = "autoscanforjavaci";
  private static final String ORG_AUTOSCAN_FOR_JAVA_FORK = "autoscanforjavafork";

  public static void main(String[] args) {

    ApiConnector apiConnector = new ApiConnector(SC_STAGING_URL);
    ProjectAnalysis projectAnalysis = new ProjectAnalysis(apiConnector);

    List<Component> baseComponents = apiConnector.getOrganizationProjects(ORG_AUTOSCAN_FOR_JAVA_CI);
    List<Component> targetComponents = apiConnector.getOrganizationProjects(ORG_AUTOSCAN_FOR_JAVA_FORK);

    List<ProjectAnalysisQuality> projectsQuality = baseComponents.stream()
      .map(projectAnalysis::toAnalysisQuality)
      .map(projectAnalysis::extractResult)
      .map(pq -> projectAnalysis.extractTargetResult(pq, targetComponents))
      .filter(ProjectAnalysisQuality::hasTarget)
      .map(projectAnalysis::processDifferences)
      .collect(Collectors.toList());

    // TODO: Retrieve dependencies resolution
    // TODO: refine quality results (only issues differences? plugin version? quality profile?)
    // TODO: export quality results
  }
}
