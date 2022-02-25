package model;

import java.util.Objects;

import java.util.Date;

public class Component {

  private String organization;
  private String key;
  private String name;
  private String qualifier;
  private String language;
  private Date analysisDate;
  private String eligibilityStatus;
  private boolean eligible;
  private String comparableKey;

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public String getKey() {
    return key;
  }

  public String getComparableKey() {
    if (this.comparableKey == null) {
      this.comparableKey = key.substring(key.indexOf(":"));
    }
    return comparableKey;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getQualifier() {
    return qualifier;
  }

  public void setQualifier(String qualifier) {
    this.qualifier = qualifier;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public Date getAnalysisDate() {
    return analysisDate;
  }

  public void setAnalysisDate(Date analysisDate) {
    this.analysisDate = analysisDate;
  }

  public String getEligibilityStatus() {
    return eligibilityStatus;
  }

  public void setEligibilityStatus(String eligibilityStatus) {
    this.eligibilityStatus = eligibilityStatus;
  }

  public boolean isEligible() {
    return eligible;
  }

  public void setEligible(boolean eligible) {
    this.eligible = eligible;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Component component = (Component) o;
    return Objects.equals(getComparableKey(), component.getComparableKey());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getComparableKey());
  }
}
