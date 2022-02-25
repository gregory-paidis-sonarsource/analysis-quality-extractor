package model.logs;

import com.google.gson.annotations.SerializedName;

public class CloudWatchEntry {

  @SerializedName("_aws")
  private CloudWatchMetrics cloudWatchMetrics;

  @SerializedName("cuuid")
  private String cuuid;

  @SerializedName("alm_repo")
  private String almRepo;

  @SerializedName("branch")
  private String branch;

  @SerializedName("project")
  private String projectKey;

  @SerializedName("project_uuid")
  private String projectUuid;

  public CloudWatchMetrics getCloudWatchMetrics() {
    return cloudWatchMetrics;
  }

  public void setCloudWatchMetrics(CloudWatchMetrics cloudWatchMetrics) {
    this.cloudWatchMetrics = cloudWatchMetrics;
  }

  public String getCuuid() {
    return cuuid;
  }

  public void setCuuid(String cuuid) {
    this.cuuid = cuuid;
  }

  public String getAlmRepo() {
    return almRepo;
  }

  public void setAlmRepo(String almRepo) {
    this.almRepo = almRepo;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }

  public String getProjectUuid() {
    return projectUuid;
  }

  public void setProjectUuid(String projectUuid) {
    this.projectUuid = projectUuid;
  }
}
