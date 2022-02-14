package model;

public class ProjectAnalysisQuality {

  private Component baseComponent;
  private String baseComponentDefaultBranch;
  private ProjectAnalysisResult baseComponentResult;
  private Component targetComponent;
  private String targetComponentDefaultBranch;
  private ProjectAnalysisResult targetComponentResult;
  private ProjectAnalysisDifferences differences;

  public boolean hasTarget() {
    return this.targetComponent != null;
  }

  public Component getBaseComponent() {
    return baseComponent;
  }

  public void setBaseComponent(Component baseComponent) {
    this.baseComponent = baseComponent;
  }

  public String getBaseComponentDefaultBranch() {
    return baseComponentDefaultBranch;
  }

  public void setBaseComponentDefaultBranch(String baseComponentDefaultBranch) {
    this.baseComponentDefaultBranch = baseComponentDefaultBranch;
  }

  public Component getTargetComponent() {
    return targetComponent;
  }

  public ProjectAnalysisQuality setTargetComponent(Component targetComponent) {
    this.targetComponent = targetComponent;
    return this;
  }

  public String getTargetComponentDefaultBranch() {
    return targetComponentDefaultBranch;
  }

  public ProjectAnalysisQuality setTargetComponentDefaultBranch(String targetComponentDefaultBranch) {
    this.targetComponentDefaultBranch = targetComponentDefaultBranch;
    return this;
  }

  public ProjectAnalysisDifferences getDifferences() {
    return differences;
  }

  public ProjectAnalysisQuality setDifferences(ProjectAnalysisDifferences differences) {
    this.differences = differences;
    return this;
  }

  public ProjectAnalysisResult getBaseComponentResult() {
    return baseComponentResult;
  }

  public ProjectAnalysisQuality setBaseComponentResult(ProjectAnalysisResult baseComponentResult) {
    this.baseComponentResult = baseComponentResult;
    return this;
  }

  public ProjectAnalysisResult getTargetComponentResult() {
    return targetComponentResult;
  }

  public ProjectAnalysisQuality setTargetComponentResult(ProjectAnalysisResult targetComponentResult) {
    this.targetComponentResult = targetComponentResult;
    return this;
  }

  @Override
  public String toString() {
    return "Project " + baseComponent.getName() + " has " + targetComponentResult.getIssues().size() + " issues " +
      "(" + differences.getAdded().size() + " added / " + differences.getMissing().size() + " missing)";
  }
}
