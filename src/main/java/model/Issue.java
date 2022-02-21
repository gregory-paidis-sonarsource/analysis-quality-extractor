package model;

import java.util.Objects;

public class Issue {

  private String key;
  private String rule;
  private String severity;
  private String status;
  private String type;
  private TextRange textRange;
  private String component;
  private String comparableComponent;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public TextRange getTextRange() {
    return textRange;
  }

  public void setTextRange(TextRange textRange) {
    this.textRange = textRange;
  }

  public String getComponent() {
    return component;
  }

  public void setComponent(String component) {
    this.component = component;
  }

  public String getComparableComponent() {
    if (comparableComponent == null) {
      comparableComponent = component.substring(component.indexOf(":"));
    }
    return comparableComponent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Issue issue = (Issue) o;
    if (textRange != null && issue.textRange != null) {
      return rule.equals(issue.rule) && textRange.equals(issue.textRange) && getComparableComponent().equals(issue.getComparableComponent());
    }
    if (textRange == null && issue.textRange != null) {
      return false;
    }
    if (textRange != null && issue.textRange == null) {
      return false;
    }
    return rule.equals(issue.rule) && getComparableComponent().equals(issue.getComparableComponent());
  }

  @Override
  public int hashCode() {
    return Objects.hash(rule, textRange, getComparableComponent());
  }

  @Override
  public String toString() {
    return String.format(
      "Component %s Issue %s Rule %s Range %s",
      getComponent(), getKey(), getRule(), getTextRangeReadable());
  }

  private String getTextRangeReadable() {
    if (getTextRange() == null) {
      return "(empty)";
    }

    return String.format("%d:%d:%d:%d",
      getTextRange().getStartLine(), getTextRange().getEndLine(),
      getTextRange().getStartOffset(), getTextRange().getEndOffset());
  }
}
