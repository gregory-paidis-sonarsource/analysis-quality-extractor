package task;

import com.google.gson.Gson;
import extractor.ProjectAnalysis;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.ProjectAnalysisQuality;

import static task.AnalysisQualityLocComparison.compareLoc;

public class AnalysisResultFromFile {

  public static final String OUTPUT_FOLDER = "src/main/output_issues/";
  public static final String OUTPUT_FOLDER_BASE = OUTPUT_FOLDER + "base/";
  public static final String OUTPUT_FOLDER_TARGET = OUTPUT_FOLDER + "target/";
  public static final String AUTOSCAN_SUFFIX = "autoscan";

  public static void main(String[] args) throws IOException {
    Gson gson = new Gson();

    List<ProjectAnalysisQuality> baseQualities = new ArrayList<>();

    for (File file : new File(OUTPUT_FOLDER_BASE).listFiles()) {
      ProjectAnalysisQuality object = gson.fromJson(new FileReader(file), ProjectAnalysisQuality.class);
      baseQualities.add(object);
    }

    List<ProjectAnalysisQuality> targetQualities = new ArrayList<>();

    for (File file : new File(OUTPUT_FOLDER_TARGET).listFiles()) {
      ProjectAnalysisQuality object = gson.fromJson(new FileReader(file), ProjectAnalysisQuality.class);
      targetQualities.add(object);
    }

    System.out.println("import done");

    baseQualities.forEach(paq -> {
      String name = paq.getBaseComponent().getName();
      String targetName = getTargetName(name);

      targetQualities.stream()
        .filter(target -> target.getBaseComponent().getName().equalsIgnoreCase(targetName))
        .findFirst()
        .ifPresent(target -> {
          paq.setTargetComponent(target.getBaseComponent());
          paq.setTargetComponentDefaultBranch(target.getBaseComponentDefaultBranch());
          paq.setTargetComponentResult(target.getBaseComponentResult());
        });
    });

    System.out.println("matching done");

    List<ProjectAnalysisQuality> projectsQuality = baseQualities.stream()
      .filter(ProjectAnalysisQuality::hasTarget)
      .map(ProjectAnalysis::processDifferences)
      .collect(Collectors.toList());

    System.out.println("Diff processing done");

    try {
      AnalysisQualityProcessing analysisQualityProcessing = new AnalysisQualityProcessing(false, "src/main/output/");
      analysisQualityProcessing.process(projectsQuality);

      AnalysisQualityProcessing analysisQualityProcessing2 = new AnalysisQualityProcessing(true, "src/main/output_commons/");
      analysisQualityProcessing2.process(projectsQuality);
      compareLoc(projectsQuality);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String getTargetName(String project){
    return project + "-" + AUTOSCAN_SUFFIX;
  }
}
