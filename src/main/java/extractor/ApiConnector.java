package extractor;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import model.Component;
import model.ComponentIssues;
import model.ComponentSearchProjects;
import model.ComponentTree;
import model.Issue;
import model.NavigationComponent;
import model.PluginsInstalled;
import model.ProjectBranch;
import model.ProjectBranches;

import static java.util.logging.Level.WARNING;

public class ApiConnector {

  private static final Logger LOGGER = Logger.getLogger(ApiConnector.class.getName());

  public static final int PAGE_SIZE = 500;

  private static final String API_COMPONENTS_TREE = "/api/components/tree";
  private static final String API_COMPONENTS_SEARCH_PROJECTS = "/api/components/search_projects";
  private static final String API_ISSUES_SEARCH = "/api/issues/search";
  private static final String API_SERVER_VERSION = "/api/server/version";
  private static final String API_PLUGINS_INSTALLED = "/api/plugins/installed";
  private static final String API_NAVIGATION_COMPONENT = "/api/navigation/component";
  private static final String API_PROJECT_BRANCHES_LIST = "/api/project_branches/list";

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

  public List<Component> getAllComponents(String projectKey, String branch) {
    int page = 1;
    List<Component> components = new ArrayList<>();
    do {
      Optional<ComponentTree> tree = getComponentTree(page, projectKey, branch);
      tree.ifPresent(componentTree ->
        components.addAll(componentTree.getComponents())
      );
      page++;
      if (tree.isPresent() && tree.get().getComponents().isEmpty()) {
        page = -1;
      }
    } while (page > 1);

    return components;
  }

  private Optional<ComponentTree> getComponentTree(int page, String projectKey, String branch) {
    URI uri = createURI(baseUrl, API_COMPONENTS_TREE, renderComponentTreePath(page, projectKey, branch));
    return Optional.ofNullable(GSON.fromJson(doHttpRequest(uri), ComponentTree.class));
  }

  private String renderComponentTreePath(int page, String projectKey, String branch) {
    // TODO: create a url factory using request object
    return "ps=" + PAGE_SIZE + "&component=" +
      projectKey + "&p=" + page + "&branch=" + branch + "&qualifiers=FIL";
  }

  public List<Issue> getAllComponentIssues(String componentKeys) {
    int page = 1;
    int total = 0;
    List<Issue> issues = new ArrayList<>();
    // TODO: Better pagination
    // TODO: Consolidate pagination above extractor.ApiConnector.getAllComponents
    do {
      Optional<ComponentIssues> componentIssues = getComponentIssues(page++, componentKeys);
      if (componentIssues.isPresent() && componentIssues.get().getIssues() != null) {
        if (componentIssues.get().getIssues().isEmpty()) {
          break;
        }
        List<Issue> i = componentIssues.get().getIssues();
        if (i != null) {
          issues.addAll(i);
          total += issues.size();
          if (total >= componentIssues.get().getTotal()) {
            break;
          }
        }
      }
    } while (true);

    return issues;
  }

  private Optional<ComponentIssues> getComponentIssues(int page, String componentKeys) {
    URI uri = createURI(baseUrl, API_ISSUES_SEARCH, "ps=" + PAGE_SIZE + "&componentKeys=" + componentKeys + "&p=" + page);
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
    URI uri = createURI(baseUrl, API_NAVIGATION_COMPONENT, "component=" + projectKey);
    return Optional.ofNullable(GSON.fromJson(doHttpRequest(uri), NavigationComponent.class));
  }

  public List<Component> getOrganizationProjects(String organization) {
    URI uri = createURI(baseUrl, API_COMPONENTS_SEARCH_PROJECTS, "ps=500&organization=" + organization);
    return GSON.fromJson(doHttpRequest(uri), ComponentSearchProjects.class).getComponents();
  }

  public ProjectBranch getDefaultBranch(String projectKey) {
    URI uri = createURI(baseUrl, API_PROJECT_BRANCHES_LIST, "project=" + projectKey);
    List<ProjectBranch> projectBranches = GSON.fromJson(doHttpRequest(uri), ProjectBranches.class).getBranches();
    return projectBranches.stream()
      .filter(ProjectBranch::isMain)
      .findFirst()
      .orElseThrow();
  }

  private String doHttpRequest(URI uri) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(uri)
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
    return URI.create(host + path + "?" + query.replace(" ", "%20"));
  }

}
