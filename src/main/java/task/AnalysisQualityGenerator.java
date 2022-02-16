package task;

import extractor.ApiConnector;
import extractor.ProjectAnalysis;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import model.Component;
import model.ProjectAnalysisQuality;

import static task.AnalysisQualityProcessing.processAnalysisQuality;

public class AnalysisQualityGenerator {

  private static final String SC_STAGING_URL = "https://squad-1-core.sc-dev.io";
  private static final String ORG_AUTOSCAN_FOR_JAVA_CI = "autoscanforjavaci";
  private static final String ORG_AUTOSCAN_FOR_JAVA_FORK = "autoscanforjavafork";


  private static final Set<String> FILTERED_PROJECTS = new HashSet<>(Arrays.asList(
    // AltoroJ is used to populate cache and is not eligible to autoscan
    "AltoroJ"
  ));

  public static void main(String[] args) {

    ApiConnector apiConnector = new ApiConnector(SC_STAGING_URL);
    ProjectAnalysis projectAnalysis = new ProjectAnalysis(apiConnector);

    List<Component> baseComponents = apiConnector.getOrganizationProjects(ORG_AUTOSCAN_FOR_JAVA_CI);
    List<Component> targetComponents = apiConnector.getOrganizationProjects(ORG_AUTOSCAN_FOR_JAVA_FORK);

    List<ProjectAnalysisQuality> projectsQuality = baseComponents.stream()
      .filter(c -> !FILTERED_PROJECTS.contains(c.getName()))
      .map(projectAnalysis::toAnalysisQuality)
      .map(projectAnalysis::extractResult)
      // When a project is not yet analyzed in the CI, we do not consider it.
      .filter(p -> !p.getBaseComponentResult().getIssues().isEmpty())
      .map(pq -> projectAnalysis.extractTargetResult(pq, targetComponents))
      .filter(ProjectAnalysisQuality::hasTarget)
      .map(projectAnalysis::processDifferences)
      .collect(Collectors.toList());

    projectsQuality.forEach(System.out::println);

//    try {
//      processAnalysisQuality(projectsQuality);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    // TODO: Retrieve dependencies resolution
    // TODO: refine quality results (only issues differences? plugin version? quality profile?)
  }
}
