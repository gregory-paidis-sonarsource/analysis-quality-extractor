package task;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Component;
import model.ProjectAnalysisQuality;
import model.ProjectAnalysisResult;

public class AnalysisQualityLocComparison {

  private static final String OUTPUT_FOLDER = "src/main/output/loc/";
  private static final String LANGUAGE_MONIKER = "cs";

  private AnalysisQualityLocComparison() {
  }

  static void compareLoc(List<ProjectAnalysisQuality> projectsQuality) throws IOException {
    Files.createDirectories(Paths.get(OUTPUT_FOLDER));
    FileWriter fileWriter = new FileWriter(OUTPUT_FOLDER + "comparison");
    PrintWriter printWriter = new PrintWriter(fileWriter);

    printWriter.println(
        "Project;Loc in base (CI); Loc in target (autoscan); Additional files in autoscan; additional files in CI; Additional files in autoscan (only lang-specific); additional files in CI (only lang-specific)");

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

    List<Component> missingOnlyLangSpecific = missing.stream()
        .filter(AnalysisQualityLocComparison::isLanguageSpecific)
        .collect(Collectors.toList());

    List<Component> addedOnlyLangSpecific = added.stream()
        .filter(AnalysisQualityLocComparison::isLanguageSpecific)
        .collect(Collectors.toList());

    return String.format("%s;%d;%d;%d;%d;%d;%d",
        p.getBaseComponent().getKey(),
        baseLoc.getOrDefault(LANGUAGE_MONIKER, 0),
        targetLoc.getOrDefault(LANGUAGE_MONIKER, 0),
        added.size(),
        missing.size(),
        addedOnlyLangSpecific.size(),
        missingOnlyLangSpecific.size());
  }

  private static boolean isLanguageSpecific(Component c) {
    var lang = c.getLanguage();
    return lang != null && lang.equals(LANGUAGE_MONIKER);
  }
}
