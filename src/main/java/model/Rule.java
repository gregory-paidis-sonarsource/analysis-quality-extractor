package model;

public class Rule {

  String key;

  public Rule(String repositoryKey, String key, String priority) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

}
