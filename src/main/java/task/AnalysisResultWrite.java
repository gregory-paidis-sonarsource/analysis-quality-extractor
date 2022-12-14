package task;

import com.google.gson.Gson;
import extractor.ApiConnector;
import extractor.ProjectAnalysis;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import model.Component;
import model.ProjectAnalysisQuality;

import static task.AnalysisResultFromFile.OUTPUT_FOLDER_BASE;
import static task.AnalysisResultFromFile.OUTPUT_FOLDER_TARGET;
import static task.AnalysisResultFromFile.AUTOSCAN_SUFFIX;

public class AnalysisResultWrite {
  private static final String SQ_INSTANCE_URL = "https://peach.sonarsource.com/";

  private static final List<String> projects = List.of(
    "akka",
    "auto-mapper",
    "beyourmarket",
    "codehub",
    "embedio",
    "event-flow",
    "fluentassertions",
    "flurl",
    "fody",
    "jabbr",
    "lucenenet",
    "masstransit",
    "moq",
    "net5-solution",
    "net6-solution",
    "net7-solution",
    "nhibernate",
    "nlog",
    "nodatime",
    "nopowershell",
    "northwind-traders",
    "nuget-server",
    "ocelot",
    "obsidian",
    "odata",
    "openiddict-core",
    "orchard-cms",
    "omnisharp-roslyn",
    "omnisharp-server",
    "pascalabcnet"
  );

  public static void main(String[] args) throws IOException {
    ExtractStatistics(true);
    ExtractStatistics(false);
  }

  private static void ExtractStatistics(boolean isBase) throws IOException {
    ApiConnector apiConnector = new ApiConnector(SQ_INSTANCE_URL);
    ProjectAnalysis projectAnalysis = new ProjectAnalysis(apiConnector, null);

    String outputFolder = isBase ? OUTPUT_FOLDER_BASE : OUTPUT_FOLDER_TARGET;

    List<Component> components = apiConnector.getProjects(getProjectPaths(isBase));

    Gson gson = new Gson();
    Files.createDirectories(Paths.get(outputFolder));

    ExecutorService executor = Executors.newFixedThreadPool(10);
    for (Component component : components) {
      executor.submit(() -> {
        try {
          System.out.println("Running for project key: " + component.getKey());
          ProjectAnalysisQuality paq = projectAnalysis.toAnalysisQuality(component);
          ProjectAnalysisQuality paqExtracted = projectAnalysis.extractResult(paq);
          try (FileWriter fileWriter = new FileWriter(
              outputFolder + paqExtracted.getBaseComponent().getKey().replace(":", "_"))) {
            gson.toJson(paqExtracted, fileWriter);
          }
        } catch (Exception e) {
          System.err.println("[ERROR] Fail to download " + component + ": " + e.getMessage());
        }
      });

    }
    executor.shutdown();
  }

  private static List<String> getProjectPaths(boolean isBase) {
    if (isBase) {
      return projects;
    }

    return projects.stream()
        .map(s -> s + "-" + AUTOSCAN_SUFFIX)
        .collect(Collectors.toList());
  }
}
