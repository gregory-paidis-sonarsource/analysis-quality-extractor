package extractor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import model.Component;
import model.Issue;
import model.ProjectAnalysisDifferences;
import model.ProjectAnalysisQuality;
import model.ProjectAnalysisResult;
import model.QualityProfile;

import static java.util.logging.Level.INFO;

public class ProjectAnalysis {

  private static final Logger LOGGER = Logger.getLogger(ProjectAnalysis.class.getName());

  private final ApiConnector apiConnector;

  public ProjectAnalysis(ApiConnector apiConnector, MetricsConnector metricsConnector) {
    this.apiConnector = apiConnector;
  }

  public ProjectAnalysisResult extractResult(String projectKey) {
    // This does not fetch project/assembly level issues, 
    // so the number might be less in contrast to peachy issues/ tab
    int pageSize = 5;
    long start = System.currentTimeMillis();

    List<Issue> issues = new ArrayList<>();
    List<Component> mainComponents = apiConnector.getAllComponents(projectKey, "FIL");
    for (int i = 0; i < mainComponents.size(); i += pageSize) {
      var componentKeys = mainComponents.subList(i, Math.min(i + pageSize, mainComponents.size())).stream()
        .map(Component::getKey).collect(Collectors.toList());
      componentKeys.replaceAll(s -> URLEncoder.encode(s, StandardCharsets.UTF_8));
      String componentQuery = String.join(",", componentKeys);
      List<Issue> allComponentIssues = apiConnector.getAllComponentIssues(componentQuery);
      issues.addAll(allComponentIssues);
    }

    List<Component> testComponents = apiConnector.getAllComponents(projectKey, "UTS");
    for (int i = 0; i < testComponents.size(); i += pageSize) {
      String componentKeys = testComponents.subList(i, Math.min(i + pageSize, testComponents.size())).stream()
        .map(Component::getKey)
        .collect(Collectors.joining(","));
      issues.addAll(apiConnector.getAllComponentIssues(componentKeys));
    }

    ProjectAnalysisResult result = new ProjectAnalysisResult()
        .setIssues(issues)
        .setServerVersion(apiConnector.getServerVersion());

    apiConnector.getPluginsInstalled()
        .ifPresent(result::setPluginsInstalled);
    apiConnector.getNavigationComponent(projectKey)
        .ifPresent(nc -> result.setQualityProfiles(extractRulesFromQualityProfiles(nc.getQualityProfiles())));

    result.setLocPerLanguages(apiConnector.getLocPerLanguages(projectKey));
    mainComponents.addAll(testComponents);
    result.setComponents(mainComponents);

    long elapsed = (System.currentTimeMillis() - start) / 1000;
    LOGGER.log(INFO, "[{0}] Retrieved {1} mainComponents and {2} issues in {3} seconds",
        new Object[] { projectKey, mainComponents.size(), issues.size(), elapsed });

    return result;
  }

  public static ProjectAnalysisDifferences processDifferences(ProjectAnalysisResult base,
      ProjectAnalysisResult target) {
    Set<Issue> baseIssues = new HashSet<>(base.getIssues());
    Set<Issue> targetIssues = new HashSet<>(target.getIssues());

    List<Issue> missing = baseIssues.stream()
        .filter(issue -> !targetIssues.contains(issue))
        .collect(Collectors.toList());

    List<Issue> added = target.getIssues().stream()
        .filter(issue -> !baseIssues.contains(issue))
        .collect(Collectors.toList());

    List<Component> targetComponents = target.getComponents();

    Set<String> intersection = base.getComponents().stream()
        .distinct()
        .filter(targetComponents::contains)
        .map(Component::getComparableKey)
        .collect(Collectors.toSet());

    List<Issue> missingInBoth = missing.stream()
        .filter(i -> intersection.contains(i.getComparableComponent()))
        .collect(Collectors.toList());

    List<Issue> addedInBoth = added.stream()
        .filter(i -> intersection.contains(i.getComparableComponent()))
        .collect(Collectors.toList());

    List<Issue> baseIssuesInBoth = baseIssues.stream()
        .filter(i -> intersection.contains(i.getComparableComponent()))
        .collect(Collectors.toList());

    return new ProjectAnalysisDifferences()
        .setAdded(added)
        .setMissing(missing)
        .setAddedInCommonComponents(addedInBoth)
        .setMissingInCommonComponents(missingInBoth)
        .setBaseIssues(base.getIssues())
        .setBaseIssuesInCommonComponents(baseIssuesInBoth);
  }

  public ProjectAnalysisQuality toAnalysisQuality(Component component) {
    ProjectAnalysisQuality projectQuality = new ProjectAnalysisQuality();
    projectQuality.setBaseComponent(component);
    return projectQuality;
  }

  public ProjectAnalysisQuality extractResult(ProjectAnalysisQuality pq) {
    return pq.setBaseComponentResult(
        extractResult(
          pq.getBaseComponent().getKey()
          ));
  }

  public static ProjectAnalysisQuality processDifferences(ProjectAnalysisQuality pq) {
    return pq.setDifferences(processDifferences(
        pq.getBaseComponentResult(), pq.getTargetComponentResult()));
  }

  private List<QualityProfile> extractRulesFromQualityProfiles(List<QualityProfile> qualityProfiles) {
    qualityProfiles.forEach(qp -> qp.setRules(apiConnector.getRulesFromQualityProfile(qp)));
    return qualityProfiles;
  }
}
