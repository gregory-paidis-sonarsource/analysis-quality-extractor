package model;

import java.util.Objects;

public class TextRange {

  private int startLine;
  private int endLine;
  private int startOffset;
  private int endOffset;

  public int getStartLine() {
    return startLine;
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }

  public int getEndLine() {
    return endLine;
  }

  public void setEndLine(int endLine) {
    this.endLine = endLine;
  }

  public int getStartOffset() {
    return startOffset;
  }

  public void setStartOffset(int startOffset) {
    this.startOffset = startOffset;
  }

  public int getEndOffset() {
    return endOffset;
  }

  public void setEndOffset(int endOffset) {
    this.endOffset = endOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TextRange textRange = (TextRange) o;
    return startLine == textRange.startLine &&
      endLine == textRange.endLine &&
      startOffset == textRange.startOffset &&
      endOffset == textRange.endOffset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startLine, endLine, startOffset, endOffset);
  }
}
