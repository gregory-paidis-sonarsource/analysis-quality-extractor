package model;

import java.util.List;

public class ProjectAnalysisResult {

  private List<Issue> issues;
  private String serverVersion;
  private PluginsInstalled pluginsInstalled;
  private List<QualityProfile> qualityProfiles;

  public List<Issue> getIssues() {
    return issues;
  }

  public ProjectAnalysisResult setIssues(List<Issue> issues) {
    this.issues = issues;
    return this;
  }

  public String getServerVersion() {
    return serverVersion;
  }

  public ProjectAnalysisResult setServerVersion(String serverVersion) {
    this.serverVersion = serverVersion;
    return this;
  }

  public PluginsInstalled getPluginsInstalled() {
    return pluginsInstalled;
  }

  public ProjectAnalysisResult setPluginsInstalled(PluginsInstalled pluginsInstalled) {
    this.pluginsInstalled = pluginsInstalled;
    return this;
  }

  public List<QualityProfile> getQualityProfiles() {
    return qualityProfiles;
  }

  public ProjectAnalysisResult setQualityProfiles(List<QualityProfile> qualityProfiles) {
    this.qualityProfiles = qualityProfiles;
    return this;
  }
}
