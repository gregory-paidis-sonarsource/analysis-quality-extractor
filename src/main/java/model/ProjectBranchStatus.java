package model;

public class ProjectBranchStatus {

  private int bugs;
  private int vulnerabilites;
  private int codeSmells;

  public int getBugs() {
    return bugs;
  }

  public void setBugs(int bugs) {
    this.bugs = bugs;
  }

  public int getVulnerabilites() {
    return vulnerabilites;
  }

  public void setVulnerabilites(int vulnerabilites) {
    this.vulnerabilites = vulnerabilites;
  }

  public int getCodeSmells() {
    return codeSmells;
  }

  public void setCodeSmells(int codeSmells) {
    this.codeSmells = codeSmells;
  }
}
