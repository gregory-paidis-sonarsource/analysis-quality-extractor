package model;

import java.util.List;

public class NavigationComponent {

  private String key;
  private String organization;
  private String visibility;
  private String ciName;
  private List<QualityProfile> qualityProfiles;
  private QualityGate qualityGate;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getVisibility() {
    return visibility;
  }

  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

  public String getCiName() {
    return ciName;
  }

  public void setCiName(String ciName) {
    this.ciName = ciName;
  }

  public List<QualityProfile> getQualityProfiles() {
    return qualityProfiles;
  }

  public void setQualityProfiles(List<QualityProfile> qualityProfiles) {
    this.qualityProfiles = qualityProfiles;
  }

  public QualityGate getQualityGate() {
    return qualityGate;
  }

  public void setQualityGate(QualityGate qualityGate) {
    this.qualityGate = qualityGate;
  }
}
