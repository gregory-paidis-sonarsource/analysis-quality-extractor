package extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  public ProjectAnalysis(ApiConnector apiConnector) {
    this.apiConnector = apiConnector;
  }

  public ProjectAnalysisResult extractResult(String projectKey, String branch) {
    int pageSize = 50;
    long start = System.currentTimeMillis();

    List<Issue> issues = new ArrayList<>();
    List<Component> components = apiConnector.getAllComponents(projectKey, branch);

    IntStream.range(0, components.size())
      .filter(i -> i % pageSize == 0)
      .forEach(i -> {
        String componentKeys = components.subList(i, Math.min(i + pageSize - 1, components.size())).stream()
          .map(Component::getKey)
          .collect(Collectors.joining(","));
        issues.addAll(apiConnector.getAllComponentIssues(componentKeys));
      });

    ProjectAnalysisResult result = new ProjectAnalysisResult()
      .setIssues(issues)
      .setServerVersion(apiConnector.getServerVersion());

    apiConnector.getPluginsInstalled()
      .ifPresent(result::setPluginsInstalled);
    apiConnector.getNavigationComponent(projectKey)
      .ifPresent(nc -> result.setQualityProfiles(extractRulesFromQualityProfiles(nc.getQualityProfiles())));

    result.setLocPerLanguages(apiConnector.getLocPerLanguages(projectKey));

    result.setComponents(components);

    long elapsed = (System.currentTimeMillis() - start) / 1000;
    LOGGER.log(INFO, "Retrieved {0} components and {1} issues in {2} seconds",
      new Object[]{components.size(), issues.size(), elapsed});

    return result;
  }

  public ProjectAnalysisDifferences processDifferences(ProjectAnalysisResult base, ProjectAnalysisResult target) {
    List<Issue> missing = base.getIssues().stream()
      .filter(issue -> !target.getIssues().contains(issue))
      .collect(Collectors.toList());
    List<Issue> added = target.getIssues().stream()
      .filter(issue -> !base.getIssues().contains(issue))
      .collect(Collectors.toList());

    return new ProjectAnalysisDifferences()
      .setAdded(added)
      .setMissing(missing);
  }

  public ProjectAnalysisQuality toAnalysisQuality(Component component) {
    ProjectAnalysisQuality projectQuality = new ProjectAnalysisQuality();
    projectQuality.setBaseComponent(component);
    projectQuality.setBaseComponentDefaultBranch(
      apiConnector.getDefaultBranch(component.getKey()).getName());
    return projectQuality;
  }

  public ProjectAnalysisQuality extractResult(ProjectAnalysisQuality pq) {
    return pq.setBaseComponentResult(
      extractResult(
        pq.getBaseComponent().getKey(),
        pq.getBaseComponentDefaultBranch()
      ));
  }

  public ProjectAnalysisQuality extractTargetResult(ProjectAnalysisQuality pq, List<Component> availableTargets) {
    LOGGER.log(INFO, "Processing: {0}", pq.getBaseComponent().getName());
    findMatchingComponent(pq.getBaseComponent(), availableTargets).ifPresent(c ->
      pq.setTargetComponentDefaultBranch(apiConnector.getDefaultBranch(c.getKey()).getName())
        .setTargetComponent(c).setTargetComponentResult(
          extractResult(
            pq.getTargetComponent().getKey(),
            pq.getTargetComponentDefaultBranch()
          )));
    return pq;
  }

  public ProjectAnalysisQuality processDifferences(ProjectAnalysisQuality pq) {
    return pq.setDifferences(processDifferences(
      pq.getBaseComponentResult(), pq.getTargetComponentResult()
    ));
  }

  private Optional<Component> findMatchingComponent(Component base, List<Component> availableTargets) {
    return availableTargets.stream()
      .filter(component -> base.getName().equalsIgnoreCase(component.getName()))
      .findFirst();
  }

  private List<QualityProfile> extractRulesFromQualityProfiles(List<QualityProfile> qualityProfiles) {
    qualityProfiles.forEach(qp -> qp.setRules(apiConnector.getRulesFromQualityProfile(qp)));
    return qualityProfiles;
  }
}
