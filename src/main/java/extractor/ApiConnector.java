package extractor;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import model.Component;
import model.ComponentIssues;
import model.ComponentRules;
import model.ComponentSearchProjects;
import model.ComponentTree;
import model.Issue;
import model.NavigationComponent;
import model.PluginsInstalled;
import model.QualityProfile;
import model.Rule;
import model.measure.ComponentMeasure;
import model.measure.Measure;
import org.apache.http.HttpHeaders;

import static java.util.logging.Level.WARNING;

public class ApiConnector {

  private static final Logger LOGGER = Logger.getLogger(ApiConnector.class.getName());

  public static final int PAGE_SIZE = 500;
  public static final String DEFAULT_ANALYSIS_BRANCH = "master";

  private static final String API_COMPONENTS_TREE = "/api/components/tree";
  private static final String API_COMPONENTS_SEARCH = "/api/components/search";
  private static final String API_ISSUES_SEARCH = "/api/issues/search";
  private static final String API_SERVER_VERSION = "/api/server/version";
  private static final String API_PLUGINS_INSTALLED = "/api/plugins/installed";
  private static final String API_NAVIGATION_COMPONENT = "/api/navigation/component";
  private static final String API_MEASURE_COMPONENT = "/api/measures/component";
  private static final String API_RULE_SEARCH = "/api/rules/search";

  private static HashSet<String> EXCLUDED_RULES = new HashSet<String>(
    List.of(
        "S5131",
        "S2083",
        "S2078",
        "S2076",
        "S5883",
        "S6350",
        "S2631",
        "S3649",
        "S2091",
        "S5144",
        "S5145",
        "S5146",
        "S5167",
        "S5135",
        "S5334",
        "S6096",
        "S6287",
        "S2228",
        "S3900",
        "S3904",
        "S3992",
        "S1128"));

  private static final Gson GSON = new Gson();

  private final String baseUrl;
  private final HttpClient httpClient;

  public ApiConnector(String baseUrl) {
    this(baseUrl, HttpClient.newHttpClient());
  }

  public ApiConnector(String baseUrl, HttpClient httpClient) {
    this.baseUrl = baseUrl;
    this.httpClient = httpClient;
  }

  public List<Component> getAllComponents(String projectKey, String qualifier) {
    int page = 1;
    List<Component> components = new ArrayList<>();
    do {
      Optional<ComponentTree> tree = getComponentTree(page, projectKey, qualifier);
      tree.ifPresent(componentTree -> components.addAll(componentTree.getComponents()));
      page++;
      if (tree.isPresent() && tree.get().getComponents().isEmpty()) {
        page = -1;
      }
    } while (page > 1);

    return components;
  }

  private Optional<ComponentTree> getComponentTree(int page, String projectKey, String qualifier) {
    URI uri = createURI(baseUrl, API_COMPONENTS_TREE, renderComponentTreePath(page, projectKey, qualifier));
    return Optional.ofNullable(GSON.fromJson(doHttpRequest(uri), ComponentTree.class));
  }

  private String renderComponentTreePath(int page, String projectKey, String qualifier) {
    return "ps=" + PAGE_SIZE + "&component=" +
        projectKey + "&p=" + page + "&branch=" + DEFAULT_ANALYSIS_BRANCH + "&qualifiers=" + qualifier;
  }

  public List<Issue> getAllComponentIssues(String componentKeys) {
    int page = 1;
    List<Issue> totalResult = new ArrayList<>();
    do {
      Optional<ComponentIssues> componentIssues = getComponentIssues(page++, componentKeys);
      if (componentIssues.isPresent() && componentIssues.get().getIssues() != null) {
        if (componentIssues.get().getIssues().isEmpty()) {
          break;
        }
        List<Issue> currentResult = componentIssues.get().getIssues();
        if (currentResult != null) {

          currentResult.removeIf(i -> {
            var parts = i.getRule().split(":");
            return parts.length < 2 || EXCLUDED_RULES.contains(parts[1]); 
          });

          totalResult.addAll(currentResult);
        }
      }
    } while (true);

    return totalResult;
  }

  private Optional<ComponentIssues> getComponentIssues(int page, String componentKeys) {
    URI uri = createURI(baseUrl, API_ISSUES_SEARCH,
            "ps=" + PAGE_SIZE +
            "&components=" + componentKeys +
            "&p=" + page +
            "&resolved=false" +
            "&branch=" + DEFAULT_ANALYSIS_BRANCH);
    return Optional.ofNullable(GSON.fromJson(doHttpRequest(uri), ComponentIssues.class));
  }

  public String getServerVersion() {
    return doHttpRequest(URI.create(baseUrl + API_SERVER_VERSION));
  }

  public Optional<PluginsInstalled> getPluginsInstalled() {
    URI uri = createURI(baseUrl, API_PLUGINS_INSTALLED, "");
    return Optional.ofNullable(GSON.fromJson(doHttpRequest(uri), PluginsInstalled.class));
  }

  public Optional<NavigationComponent> getNavigationComponent(String projectKey) {
    URI uri = createURI(baseUrl, API_NAVIGATION_COMPONENT, "component=" + projectKey + "&branch=" + DEFAULT_ANALYSIS_BRANCH);
    return Optional.ofNullable(GSON.fromJson(doHttpRequest(uri), NavigationComponent.class));
  }

  public List<Component> getProjects(List<String> organization) {
    return organization.stream()
        .map(this::getProject)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public Component getProject(String project) {
    try {
      URI uri = createURI(baseUrl, API_COMPONENTS_SEARCH, "qualifiers=TRK&q=" + project);
      return GSON.fromJson(doHttpRequest(uri), ComponentSearchProjects.class)
          .getComponents()
          .stream()
          .filter(p -> p.getKey().equals(project))
          .findFirst()
          .get();
    } catch (Exception e) {
      System.err.println("[ERROR] Fail to find project: " + project);
      return null;
    }

  }

  public List<Rule> getRulesFromQualityProfile(QualityProfile qp) {
    URI uri = createURI(baseUrl, API_RULE_SEARCH,
        "ps=" + PAGE_SIZE + "&languages=" + qp.getLanguage() + "&qprofile=" + qp.getKey() + "&activation=true");
    return GSON.fromJson(doHttpRequest(uri), ComponentRules.class).getRules();
  }

  public Map<String, Integer> getLocPerLanguages(String projectKey) {
    URI uri = createURI(baseUrl, API_MEASURE_COMPONENT,
          "component=" + projectKey +
          "&metricKeys=ncloc_language_distribution" +
          "&branch=" + DEFAULT_ANALYSIS_BRANCH);
    Map<String, Integer> locPerLanguages = new HashMap<>();
    List<Measure> measures = GSON.fromJson(doHttpRequest(uri), ComponentMeasure.class).getComponent().getMeasures();
    if (measures.isEmpty()) {
      return new HashMap<>();
    }
    String value = measures.get(0).getValue();
    Arrays.stream(value.split(";"))
        .map(pair -> pair.split("="))
        .forEach(p -> locPerLanguages.put(p[0], Integer.parseInt(p[1])));
    return locPerLanguages;
  }

  private String doHttpRequest(URI uri) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .setHeader(HttpHeaders.AUTHORIZATION, "Basic " +
            Base64.getEncoder().encodeToString((System.getenv("PEACH_TOKEN") + ":").getBytes()))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body();
    } catch (IOException | InterruptedException exception) {
      LOGGER.log(WARNING, exception.getMessage());
      return null;
    }
  }

  private URI createURI(String host, String path, String query) {
    if (query.isEmpty()) {
      return URI.create(host + path);
    }
    return URI.create(host + path + "?" + query);
  }
}
