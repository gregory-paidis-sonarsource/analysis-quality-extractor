package task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import model.Issue;
import model.ProjectAnalysisDifferences;
import model.ProjectAnalysisMetrics;
import model.ProjectAnalysisQuality;

public class AnalysisQualityProcessing {

  private static final String LANGUAGE_RULE_PREFIX = "csharpsquid:";

  private static final String HEADER = "Project name; Issues on base; FN; Detection (%); FP; Deviation (%)";
  private static final String HEADER_METRICS = "JavaAnalysisFinishedCount; WorkerForJavaCloneDuration; WorkerForJavaTaskDuration; DownloadedArtifactsPercentage; ParsedArtifactsCount; ConstructDependencyGraphDuration; DownloadDependenciesDuration; ResolveDependenciesDuration; WorkerForJavaQueueLatency";
  private static final String VULNERABILITY = "VULNERABILITY";

  private final Map<String, List<Issue>> countMissing = new HashMap<>();
  private final Map<String, List<Issue>> countAdded = new HashMap<>();

  private final List<Summary> summaryAll = new ArrayList<>();
  private final List<Summary> summaryLanguageSpecific = new ArrayList<>();

  private final Map<String, Integer> totalIssuesRaisedByRule = new HashMap<>();

  private final Function<ProjectAnalysisDifferences, List<Issue>> getMissing;
  private final Function<ProjectAnalysisDifferences, List<Issue>> getAdded;
  private final Function<ProjectAnalysisDifferences, List<Issue>> getBaseIssues;

  private final String outputFolder;
  private final String outputFolderAll;
  private final String outputFolderOnlyLangSpecific;

  public AnalysisQualityProcessing(boolean onlyCommonComponents, String outputFolder) {
    this.outputFolder = outputFolder;
    this.outputFolderAll = outputFolder + "projects_all/";
    this.outputFolderOnlyLangSpecific = outputFolder + "projects_only_lang_specific/";
    if (onlyCommonComponents) {
      getMissing = ProjectAnalysisDifferences::getMissingInCommonComponents;
      getAdded = ProjectAnalysisDifferences::getAddedInCommonComponents;
      getBaseIssues = ProjectAnalysisDifferences::getBaseIssuesInCommonComponents;
    } else {
      getMissing = ProjectAnalysisDifferences::getMissing;
      getAdded = ProjectAnalysisDifferences::getAdded;
      getBaseIssues = ProjectAnalysisDifferences::getBaseIssues;
    }
  }

  public void process(List<ProjectAnalysisQuality> projectsQuality) throws IOException {
    populateRulesKeyFromQualityProfile(totalIssuesRaisedByRule, projectsQuality);

    processAnalysisQuality(projectsQuality);

    writeNoisyRules(countAdded, countMissing);

    writeNoisyRulesWithDetails(countAdded, countMissing);

    writeRulesStatistics(totalIssuesRaisedByRule, countMissing, countAdded);

    writeSummary(summaryAll, "summary_all");
    writeSummary(summaryLanguageSpecific, "summary_lang_specific");
  }

  private void processAnalysisQuality(List<ProjectAnalysisQuality> projectsQuality) throws IOException {
    for (ProjectAnalysisQuality projectAnalysisQuality : projectsQuality) {
      summaryAll.add(generateOutputForProject(projectAnalysisQuality, false));
      summaryLanguageSpecific.add(generateOutputForProject(projectAnalysisQuality, true));
      // Compute noisy rules
      ProjectAnalysisDifferences differences = projectAnalysisQuality.getDifferences();
      getMissing.apply(differences).forEach(i -> incrementMap(countMissing, i));
      getAdded.apply(differences).forEach(i -> incrementMap(countAdded, i));
      // Store number of issues by key
      getBaseIssues.apply(differences).forEach(issue -> {
          String ruleKey = issue.getRule();
          Integer i = totalIssuesRaisedByRule.computeIfAbsent(ruleKey, k -> 0);
          totalIssuesRaisedByRule.put(ruleKey, i + 1);
        }
      );
    }
  }

  private static void populateRulesKeyFromQualityProfile(Map<String, Integer> totalIssuesRaisedByRule, List<ProjectAnalysisQuality> projectsQuality) {
    // Here we assume that the same quality profile is used for all projects
    projectsQuality.get(0).getBaseComponentResult().getQualityProfiles().forEach(qp ->
      qp.getRules().forEach(r ->
        totalIssuesRaisedByRule.put(r.getKey(), 0)));
  }

  private Summary generateOutputForProject(ProjectAnalysisQuality projectAnalysisQuality, boolean onlyLangSpecific) throws IOException {
    String name = projectAnalysisQuality.getBaseComponent().getName().replaceAll(":", "_");
    ProjectAnalysisDifferences differences = projectAnalysisQuality.getDifferences();

    List<Issue> baseIssues;
    List<Issue> added;
    List<Issue> missing;
    String folder;

    if (onlyLangSpecific) {
      baseIssues = getBaseIssues.apply(differences).stream()
        .filter(i -> i.getRule().startsWith(LANGUAGE_RULE_PREFIX))
        .collect(Collectors.toList());
      added = getAdded.apply(differences).stream()
        .filter(i -> i.getRule().startsWith(LANGUAGE_RULE_PREFIX))
        .collect(Collectors.toList());
      missing = getMissing.apply(differences).stream()
        .filter(i -> i.getRule().startsWith(LANGUAGE_RULE_PREFIX))
        .collect(Collectors.toList());

      folder = outputFolderOnlyLangSpecific;
    } else {
      baseIssues = getBaseIssues.apply(differences);

      added = getAdded.apply(differences);
      missing = getMissing.apply(differences);
      folder = outputFolderAll;
    }

    return generateOutputForProject(added, missing, baseIssues, folder, name,
      projectAnalysisQuality.getTargetComponentAnalysisMetrics());
  }

  private static Summary generateOutputForProject(List<Issue> allAdded, List<Issue> allMissing, List<Issue> baseIssues,
    String folder, String name, ProjectAnalysisMetrics metrics) throws IOException {
    PrintWriter printWriter = printWriter(folder, name);

    List<Issue> added = allAdded.stream()
      .filter(i -> !i.getType().equals(VULNERABILITY))
      .collect(Collectors.toList());
    List<Issue> missing = allMissing.stream()
      .filter(i -> !i.getType().equals(VULNERABILITY))
      .collect(Collectors.toList());

    printWriter.println("====== Bugs ======");

    String bugSummary = printIssuesDifference(added, missing, baseIssues.size(), name, printWriter);

    long baseIssuesVulnerability = baseIssues.stream()
      .filter(i -> i.getType().equals(VULNERABILITY))
      .count();
    List<Issue> addedVulnerability = allAdded.stream()
      .filter(i -> i.getType().equals(VULNERABILITY))
      .collect(Collectors.toList());
    List<Issue> missingVulnerability = allMissing.stream()
      .filter(i -> i.getType().equals(VULNERABILITY))
      .collect(Collectors.toList());
    printWriter.println("====== Vulnerabilities ======");
    String vulnerabilitySummary = printIssuesDifference(addedVulnerability, missingVulnerability, baseIssuesVulnerability, name, printWriter);
    printWriter.println("====== Metrics ======");
    String analysisMetricsSummary = printAnalysisMetrics(metrics, printWriter);

    printWriter.close();
    return new Summary(bugSummary, vulnerabilitySummary, analysisMetricsSummary);
  }

  private static String printIssuesDifference(List<Issue> added, List<Issue> missing, long baseIssues, String baseComponentName, PrintWriter printWriter) {
    int addedSize = added.size();
    int missingSize = missing.size();
    printWriter.println(HEADER);
    String summary = String.format("%s;%d;%d;%f;%d;%f",
      baseComponentName,
      baseIssues,
      missingSize,
      ((baseIssues - missingSize) / (double) baseIssues) * 100,
      addedSize,
      (((baseIssues + addedSize) * 100) / (double) baseIssues) - 100
    );
    printWriter.println(summary);

    printWriter.println("====== Added issues ======");
    prettyPrintIssuesByRuleKey(added, printWriter);
    printWriter.println();
    printWriter.println("====== Missing issues ======");
    prettyPrintIssuesByRuleKey(missing, printWriter);
    return summary;
  }


  private static void prettyPrintIssuesByRuleKey(List<Issue> issues, PrintWriter printWriter) {
    Map<String, List<Issue>> groupByRules = issues.stream().collect(Collectors.groupingBy(Issue::getRule));
    prettyPrintIssuesByRuleKey(groupByRules, printWriter);
  }

  private static void prettyPrintIssuesByRuleKey(Map<String, List<Issue>> groupByRules, PrintWriter printWriter) {
    for (Map.Entry<String, List<Issue>> entry : groupByRules.entrySet()) {
      List<Issue> issuesByKey = entry.getValue();
      printWriter.println("Rule: " + entry.getKey() + " (size: " + issuesByKey.size() + "):");
      issuesByKey.forEach(i -> printWriter.println("  " + i.toString()));
    }
  }

  private static void incrementMap(Map<String, List<Issue>> map, Issue issue) {
    List<Issue> set = map.computeIfAbsent(issue.getRule(), k -> new ArrayList<>());
    set.add(issue);
  }

  private void writeNoisyRules(Map<String, List<Issue>> countAdded, Map<String, List<Issue>> countMissing) throws IOException {
    PrintWriter printWriter = printWriter(outputFolder, "noisy_rules");

    printWriter.println("====== Noisy rules ======");
    printWriter.println("Missing rules (FN) total");
    prettyPrintNoisyRules(countMissing, printWriter);
    printWriter.println();
    printWriter.println("Added Rules (FP) total");
    prettyPrintNoisyRules(countAdded, printWriter);

    printWriter.close();
  }

  private void writeNoisyRulesWithDetails(Map<String, List<Issue>> countAdded, Map<String, List<Issue>> countMissing) throws IOException {
    PrintWriter printWriter = printWriter(outputFolder, "noisy_rules_details");

    printWriter.println("====== Noisy rules ======");
    printWriter.println("====== Missing rules (FN) total ======");
    prettyPrintIssuesByRuleKey(countMissing, printWriter);
    printWriter.println();
    printWriter.println("====== Added Rules (FP) total ======");
    prettyPrintIssuesByRuleKey(countAdded, printWriter);

    printWriter.close();
  }

  private static void prettyPrintNoisyRules(Map<String, List<Issue>> map, PrintWriter printWriter) {
    if (map.isEmpty()) {
      printWriter.println("None");
    }
    List<Map.Entry<String, List<Issue>>> collect = map.entrySet().stream().sorted((o1, o2) ->
      Integer.compare(o2.getValue().size(), o1.getValue().size()))
      .collect(Collectors.toList());

    for (Map.Entry<String, List<Issue>> entry : collect) {
      printWriter.println(entry.getKey() + " = " + entry.getValue().size());
    }
  }

  private void writeRulesStatistics(Map<String, Integer> totalIssuesRaisedByRule, Map<String, List<Issue>> countMissing, Map<String, List<Issue>> countAdded) throws IOException {
    PrintWriter printWriter = printWriter(outputFolder, "rules_summary");
    printWriter.println("Rule Key; Issues number; FN; Detection (%); FP; Deviation (%)");
    for (Map.Entry<String, Integer> entry : totalIssuesRaisedByRule.entrySet()) {
      String key = entry.getKey();
      Integer value = entry.getValue();

      int fn = countMissing.getOrDefault(key, Collections.emptyList()).size();
      int fp = countAdded.getOrDefault(key, Collections.emptyList()).size();

      String summary = String.format("%s;%d;%d;%f;%d;%f",
        key,
        entry.getValue(),
        fn,
        ((value - fn) / (double) value) * 100,
        fp,
        (((value + fp) * 100) / (double) value) - 100
      );
      printWriter.println(summary);
    }

    printWriter.close();
  }

  private static String printAnalysisMetrics(ProjectAnalysisMetrics metrics, PrintWriter printWriter) {
    printWriter.println(HEADER_METRICS);
    String summary = "No metric";
//    String summary = String.format("%d;%d;%d;%f;%d;%d;%d;%d;%d",
//      metrics.getJavaAnalysisFinishedCount(),
//      metrics.getWorkerForJavaCloneDuration(),
//      metrics.getWorkerForJavaTaskDuration(),
//      metrics.getDownloadedArtifactsPercentage(),
//      metrics.getParsedArtifactsCount(),
//      metrics.getConstructDependencyGraphDuration(),
//      metrics.getDownloadDependenciesDuration(),
//      metrics.getResolveDependenciesDuration(),
//      metrics.getWorkerForJavaQueueLatency()
//    );
    printWriter.println(summary);

    return summary;
  }

  private void writeSummary(List<Summary> summary, String fileName) throws IOException {
    PrintWriter printWriter = printWriter(outputFolder, fileName);

    printWriter.println("====== Summary ======");
    printWriter.println("====== Bugs / Code Smells ======");
    printWriter.println(HEADER);
    summary.forEach(s -> printWriter.println(s.bugSummary));

    printWriter.println("====== Vulnerabilities ======");
    printWriter.println(HEADER);
    summary.forEach(s -> printWriter.println(s.vulnerabilitySummary));

    printWriter.close();
  }

  private static PrintWriter printWriter(String folder, String fileName) throws IOException {
    Files.createDirectories(Paths.get(folder));
    FileWriter fileWriter = new FileWriter(folder + fileName);
    return new PrintWriter(fileWriter);
  }

  static class Summary {
    String bugSummary;
    String vulnerabilitySummary;
    String analysisMetrics;

    public Summary(String bugSummary, String vulnerabilitySummary, String analysisMetricsSummary) {
      this.bugSummary = bugSummary;
      this.vulnerabilitySummary = vulnerabilitySummary;
      this.analysisMetrics = analysisMetricsSummary;
    }
  }

}
