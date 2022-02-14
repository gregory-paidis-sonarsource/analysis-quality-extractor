package model;

public class ProjectBranch {

  private String name;
  private boolean isMain;
  private String type;
  private ProjectBranchStatus status;
  private String analysisDate;
  private ProjectBranchCommit commit;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isMain() {
    return isMain;
  }

  public void setMain(boolean main) {
    isMain = main;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ProjectBranchStatus getStatus() {
    return status;
  }

  public void setStatus(ProjectBranchStatus status) {
    this.status = status;
  }

  public String getAnalysisDate() {
    return analysisDate;
  }

  public void setAnalysisDate(String analysisDate) {
    this.analysisDate = analysisDate;
  }

  public ProjectBranchCommit getCommit() {
    return commit;
  }

  public void setCommit(ProjectBranchCommit commit) {
    this.commit = commit;
  }
}
