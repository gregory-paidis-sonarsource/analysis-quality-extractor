package model;

import java.util.List;

public class ProjectAnalysisDifferences {

  private List<Issue> added;
  private List<Issue> missing;
  private List<Issue> addedInCommonComponents;
  private List<Issue> missingInCommonComponents;

  private List<Issue> baseIssues;
  private List<Issue> baseIssuesInCommonComponents;

  public List<Issue> getAdded() {
    return added;
  }

  public ProjectAnalysisDifferences setAdded(List<Issue> added) {
    this.added = added;
    return this;
  }

  public List<Issue> getMissing() {
    return missing;
  }

  public ProjectAnalysisDifferences setMissing(List<Issue> missing) {
    this.missing = missing;
    return this;
  }

  public List<Issue> getAddedInCommonComponents() {
    return addedInCommonComponents;
  }

  public ProjectAnalysisDifferences setAddedInCommonComponents(List<Issue> addedInCommonComponents) {
    this.addedInCommonComponents = addedInCommonComponents;
    return this;
  }

  public List<Issue> getMissingInCommonComponents() {
    return missingInCommonComponents;
  }

  public ProjectAnalysisDifferences setMissingInCommonComponents(List<Issue> missingInCommonComponents) {
    this.missingInCommonComponents = missingInCommonComponents;
    return this;
  }

  public List<Issue> getBaseIssues() {
    return baseIssues;
  }

  public ProjectAnalysisDifferences setBaseIssues(List<Issue> baseIssues) {
    this.baseIssues = baseIssues;
    return this;
  }

  public List<Issue> getBaseIssuesInCommonComponents() {
    return baseIssuesInCommonComponents;
  }

  public ProjectAnalysisDifferences setBaseIssuesInCommonComponents(List<Issue> baseIssuesInCommonComponents) {
    this.baseIssuesInCommonComponents = baseIssuesInCommonComponents;
    return this;
  }
}
