package task;

import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import extractor.ApiConnector;
import extractor.MetricsConnector;
import extractor.ProjectAnalysis;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import model.Component;
import model.ProjectAnalysisQuality;

import static task.AnalysisQualityLocComparison.compareLoc;

public class AnalysisQualityGenerator {

  private static final String SC_STAGING_URL = "https://squad-1-core.sc-dev.io";
  private static final String ORG_AUTOSCAN_FOR_JAVA_CI = "autoscanforjavaci";
  private static final String ORG_AUTOSCAN_FOR_JAVA_FORK = "autoscanforjavafork";
  private static final Set<String> FILTERED_PROJECTS = new HashSet<>(Arrays.asList(
    // [EMPTY] no java files in src/main or src/test but src/com/ibm/security/...
    "AltoroJ",

    // [EMPTY] no java files in src/main or src/test but src/com/sectooladdict/...
    "wavsep",

    // [OOM] analysis has failed at SCM publisher stage, the first time it is executed on this project, the SCM publisher
    // requires more memory than available, but it works fine locally with more memory
    "artemis",

    // [OOM] analysis has failed, too many dependencies (> 1G)
    "io.fabric8-fabric8-maven-plugin-build",

    // Not eligible because the repository has a "sonar-project.properties" file
    "jhipster",
    "jhipster-sample-app",
    "org.codehaus.sonar-plugins-sonar-sonargraph-plugin",

    // Simple projects without any java files
    "org.assertj-assertj-parent-pom",
    "spring-velocity-support"
  ));

  public static void main(String[] args) {

    AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
    MetricsConnector cloudConnector = new MetricsConnector(logsClient);
    ApiConnector apiConnector = new ApiConnector(SC_STAGING_URL);
    ProjectAnalysis projectAnalysis = new ProjectAnalysis(apiConnector, cloudConnector);

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
      .map(projectAnalysis::extractMetrics)
      .map(projectAnalysis::processDifferences)
      .collect(Collectors.toList());

    // projectsQuality.forEach(System.out::println);

    try {
      AnalysisQualityProcessing analysisQualityProcessing = new AnalysisQualityProcessing(false, "src/main/output/");
      analysisQualityProcessing.process(projectsQuality);

      AnalysisQualityProcessing analysisQualityProcessing2 = new AnalysisQualityProcessing(true, "src/main/output_commons/");
      analysisQualityProcessing2.process(projectsQuality);
      compareLoc(projectsQuality);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
