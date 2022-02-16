package task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Issue;
import model.ProjectAnalysisDifferences;
import model.ProjectAnalysisQuality;

public class AnalysisQualityProcessing {

  private static final String JAVA_RULE_PREFIX = "java:";

  private static final String HEADER = "Project name; Issues on base; FN; Detection (%); FP; Deviation (%)";
  private static final String VULNERABILITY = "VULNERABILITY";
  private static final String OUTPUT_FOLDER = "src/main/output/";
  private static final String OUTPUT_FOLDER_ALL = OUTPUT_FOLDER + "projects_all/";
  private static final String OUTPUT_FOLDER_ONLY_JAVA = OUTPUT_FOLDER + "projects_only_java/";

  private AnalysisQualityProcessing() {
  }

  static void processAnalysisQuality(List<ProjectAnalysisQuality> projectsQuality) throws IOException {
    Map<String, Integer> totalIssuesRaisedByRule = new HashMap<>();
    populateRulesKeyFromQualityProfile(totalIssuesRaisedByRule, projectsQuality);

    Map<String, List<Issue>> countMissing = new HashMap<>();
    Map<String, List<Issue>> countAdded = new HashMap<>();

    List<Summary> summaryAll = new ArrayList<>();
    List<Summary> summaryJava = new ArrayList<>();

    for (ProjectAnalysisQuality projectAnalysisQuality : projectsQuality) {
      summaryAll.add(generateOutputForProject(projectAnalysisQuality, false));
      summaryJava.add(generateOutputForProject(projectAnalysisQuality, true));
      // Compute noisy rules
      ProjectAnalysisDifferences differences = projectAnalysisQuality.getDifferences();
      differences.getMissing().forEach(i -> incrementMap(countMissing, i));
      differences.getAdded().forEach(i -> incrementMap(countAdded, i));
      // Store number of issues by key
      projectAnalysisQuality.getBaseComponentResult().getIssues().forEach(issue -> {
          String ruleKey = issue.getRule();
          Integer i = totalIssuesRaisedByRule.computeIfAbsent(ruleKey, k -> 0);
          totalIssuesRaisedByRule.put(ruleKey, i + 1);
        }
      );
    }

    writeNoisyRules(countAdded, countMissing);

    writeNoisyRulesWithDetails(countAdded, countMissing);

    writeRulesStatistics(totalIssuesRaisedByRule, countMissing, countAdded);

    writeSummary(summaryAll, "summary_all");
    writeSummary(summaryJava, "summary_java");
  }

  private static void populateRulesKeyFromQualityProfile(Map<String, Integer> totalIssuesRaisedByRule, List<ProjectAnalysisQuality> projectsQuality) {
    // Here we assume that the same quality profile is used for all projects
    projectsQuality.get(0).getBaseComponentResult().getQualityProfiles().forEach(qp ->
      qp.getRules().forEach(r ->
        totalIssuesRaisedByRule.put(r.getKey(), 0)));
  }

  private static Summary generateOutputForProject(ProjectAnalysisQuality projectAnalysisQuality, boolean onlyJava) throws IOException {
    String name = projectAnalysisQuality.getBaseComponent().getName();
    ProjectAnalysisDifferences differences = projectAnalysisQuality.getDifferences();

    List<Issue> baseIssues;
    List<Issue> added;
    List<Issue> missing;
    String folder;

    if (onlyJava) {
      baseIssues = projectAnalysisQuality.getBaseComponentResult().getIssues().stream()
        .filter(i -> i.getRule().startsWith(JAVA_RULE_PREFIX))
        .collect(Collectors.toList());
      added = differences.getAdded().stream()
        .filter(i -> i.getRule().startsWith(JAVA_RULE_PREFIX))
        .collect(Collectors.toList());
      missing = differences.getMissing().stream()
        .filter(i -> i.getRule().startsWith(JAVA_RULE_PREFIX))
        .collect(Collectors.toList());

      folder = OUTPUT_FOLDER_ONLY_JAVA;
    } else {
      baseIssues = projectAnalysisQuality.getBaseComponentResult().getIssues();

      added = differences.getAdded();
      missing = differences.getMissing();
      folder = OUTPUT_FOLDER_ALL;
    }

    return generateOutputForProject(added, missing, baseIssues, folder, name);
  }

  private static Summary generateOutputForProject(List<Issue> allAdded, List<Issue> allMissing, List<Issue> baseIssues, String folder, String name) throws IOException {
    FileWriter fileWriter = new FileWriter(folder + name);
    PrintWriter printWriter = new PrintWriter(fileWriter);

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

    printWriter.close();
    return new Summary(bugSummary, vulnerabilitySummary);
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

  private static void writeNoisyRules(Map<String, List<Issue>> countAdded, Map<String, List<Issue>> countMissing) throws IOException {
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + "noisy_rules");
    PrintWriter printWriter = new PrintWriter(fileWriter);

    printWriter.println("====== Noisy rules ======");
    printWriter.println("Missing rules (FN) total");
    prettyPrintNoisyRules(countMissing, printWriter);
    printWriter.println();
    printWriter.println("Added Rules (FP) total");
    prettyPrintNoisyRules(countAdded, printWriter);

    printWriter.close();
  }

  private static void writeNoisyRulesWithDetails(Map<String, List<Issue>> countAdded, Map<String, List<Issue>> countMissing) throws IOException {
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + "noisy_rules_details");
    PrintWriter printWriter = new PrintWriter(fileWriter);

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

  private static void writeRulesStatistics(Map<String, Integer> totalIssuesRaisedByRule, Map<String, List<Issue>> countMissing, Map<String, List<Issue>> countAdded) throws IOException {
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + "rules_summary");
    PrintWriter printWriter = new PrintWriter(fileWriter);
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

  private static void writeSummary(List<Summary> summary, String fileName) throws IOException {
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + fileName);
    PrintWriter printWriter = new PrintWriter(fileWriter);

    printWriter.println("====== Summary ======");
    printWriter.println("====== Bugs / Code Smells ======");
    printWriter.println(HEADER);
    summary.forEach(s -> printWriter.println(s.bugSummary));

    printWriter.println("====== Vulnerabilities ======");
    printWriter.println(HEADER);
    summary.forEach(s -> printWriter.println(s.vulnerabilitySummary));

    printWriter.close();
  }

  static class Summary {
    String bugSummary;
    String vulnerabilitySummary;

    public Summary(String bugSummary, String vulnerabilitySummary) {
      this.bugSummary = bugSummary;
      this.vulnerabilitySummary = vulnerabilitySummary;
    }
  }

}
