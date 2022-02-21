package task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import model.ProjectAnalysisQuality;

public class AnalysisQualityLocComparison {

  private static final String OUTPUT_FOLDER = "src/main/output/loc/";

  private AnalysisQualityLocComparison() {
  }

  static void compareLoc(List<ProjectAnalysisQuality> projectsQuality) throws IOException {
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + "comparison");
    PrintWriter printWriter = new PrintWriter(fileWriter);

    printWriter.println("Project;Loc in base (CI); Loc in target (autoscan)");

    projectsQuality.forEach(p -> printWriter.println(getLocComparison(p)));

    printWriter.close();
  }

  private static String getLocComparison(ProjectAnalysisQuality p) {
    Map<String, Integer> baseLoc = p.getBaseComponentResult().getLocPerLanguages();
    Map<String, Integer> targetLoc = p.getTargetComponentResult().getLocPerLanguages();
    return String.format("%s;%d;%d",
      p.getBaseComponent().getKey(),
      baseLoc.getOrDefault("java", 0),
      targetLoc.getOrDefault("java", 0));
  }
}
