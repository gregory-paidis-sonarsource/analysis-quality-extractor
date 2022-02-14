package model;

import java.util.List;

public class ComponentIssues {

  private int total;
  private List<Issue> issues;

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public void setIssues(List<Issue> issues) {
    this.issues = issues;
  }
}
