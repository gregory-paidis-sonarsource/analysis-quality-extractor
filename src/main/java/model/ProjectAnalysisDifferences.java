package model;

import java.util.List;

public class ProjectAnalysisDifferences {

  private List<Issue> added;
  private List<Issue> missing;

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
}
