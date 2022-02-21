package task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Component;
import model.ProjectAnalysisQuality;
import model.ProjectAnalysisResult;

public class AnalysisQualityLocComparison {

  private static final String OUTPUT_FOLDER = "src/main/output/loc/";

  private AnalysisQualityLocComparison() {
  }

  static void compareLoc(List<ProjectAnalysisQuality> projectsQuality) throws IOException {
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + "comparison");
    PrintWriter printWriter = new PrintWriter(fileWriter);

    printWriter.println("Project;Loc in base (CI); Loc in target (autoscan); Additional files in autoscan; additional files in CI; Additional files in autoscan (only java); additional files in CI (only java)");

    projectsQuality.forEach(p -> printWriter.println(getLocComparison(p)));

    printWriter.close();
  }

  private static String getLocComparison(ProjectAnalysisQuality p) {
    ProjectAnalysisResult baseComponentResult = p.getBaseComponentResult();
    ProjectAnalysisResult targetComponentResult = p.getTargetComponentResult();

    Map<String, Integer> baseLoc = baseComponentResult.getLocPerLanguages();
    Map<String, Integer> targetLoc = targetComponentResult.getLocPerLanguages();

    List<Component> baseComponents = baseComponentResult.getComponents();
    List<Component> targetComponents = targetComponentResult.getComponents();

    List<Component> missing = baseComponents.stream()
      .filter(c -> !targetComponents.contains(c))
      .collect(Collectors.toList());
    List<Component> added = targetComponents.stream()
      .filter(c -> !baseComponents.contains(c))
      .collect(Collectors.toList());

    List<Component> missingOnlyJava = missing.stream()
      .filter(c -> c.getLanguage().equals("java"))
      .collect(Collectors.toList());
    List<Component> addedOnlyJava = added.stream()
      .filter(c -> c.getLanguage().equals("java"))
      .collect(Collectors.toList());

    return String.format("%s;%d;%d;%d;%d;%d;%d",
      p.getBaseComponent().getKey(),
      baseLoc.getOrDefault("java", 0),
      targetLoc.getOrDefault("java", 0),
      added.size(),
      missing.size(),
      addedOnlyJava.size(),
      missingOnlyJava.size()
    );
  }
}
