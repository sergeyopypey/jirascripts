import javax.ws.rs.core.Response
import groovy.transform.BaseScript
import java.util.stream.Collectors
import javax.ws.rs.core.MultivaluedMap
import com.atlassian.jira.scheme.Scheme
import com.atlassian.jira.project.Project
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.permission.PermissionSchemeManager
import com.atlassian.jira.issue.fields.config.FieldConfigScheme
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager
import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager

@BaseScript CustomEndpointDelegate delegate

issueTypeSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getComponent(IssueTypeSchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table>")
    html.append("<tr><th>Issue Type Scheme</th><th>Issue types</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")
    List<Project> resultFilteredProjects = new ArrayList<>()
    Set<FieldConfigScheme> resultFieldConfigSchemes = []

    log.warn(filteredProjects.size())
    for (Project filteredProject in filteredProjects) {
        resultFieldConfigSchemes.add(issueTypeSchemeManager.getConfigScheme(filteredProject))
    }

    for (FieldConfigScheme resultFieldConfigScheme in resultFieldConfigSchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/ConfigureOptionSchemes!default.jspa?fieldId=&schemeId=$resultFieldConfigScheme.id' >${resultFieldConfigScheme.name}</a></td>")
        html.append("<td>${issueTypeSchemeManager.getIssueTypesForScheme(resultFieldConfigScheme).collect {it.name}}</td>")
        html.append("<td>${resultFieldConfigScheme.associatedProjectObjects.collect {it.key} }</td>")
        html.append("</tr>")
    }
    log.warn(resultFilteredProjects.size())
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

permissionSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager()

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Permission Scheme</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")
    Set<Scheme> resultPermissionSchemes = []

    for (Project filteredProject in filteredProjects) {
        resultPermissionSchemes.add(permissionSchemeManager.getSchemeFor(filteredProject))
    }

    for (Scheme resultPermissionScheme in resultPermissionSchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/EditPermissions!default.jspa?schemeId=$resultPermissionScheme.id' >${resultPermissionScheme.name}</a></td>")
        html.append("<td>${permissionSchemeManager.getProjects(resultPermissionScheme)*.key}</td>")
        html.append("</tr>")
    }
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

issueSecuritySchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    IssueSecuritySchemeManager issueSecuritySchemeManager = ComponentAccessor.getComponent(IssueSecuritySchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Permission Scheme</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")
    Set<Scheme> resultIssueSecuritySchemes = []

    for (Project filteredProject in filteredProjects) {
        resultIssueSecuritySchemes.add(issueSecuritySchemeManager.getSchemeFor(filteredProject))
    }

    for (Scheme resultIssueSecurityScheme in resultIssueSecuritySchemes.sort()) {
        if (resultIssueSecurityScheme) {
            html.append("<tr>")
            html.append("<td><a href='https://jira.local/secure/admin/EditPermissions!default.jspa?schemeId=$resultIssueSecurityScheme.id' >${resultIssueSecurityScheme.name}</a></td>")
            html.append("<td>${issueSecuritySchemeManager.getProjects(resultIssueSecurityScheme)*.key}</td>")
            html.append("</tr>")
        }
    }
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

prioritySchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    PrioritySchemeManager prioritySchemeManager = ComponentAccessor.getComponent(PrioritySchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Priority Scheme</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")
    Set<FieldConfigScheme> resultPrioritySchemes = []

    for (Project filteredProject in filteredProjects) {
        resultPrioritySchemes.add(prioritySchemeManager.getScheme(filteredProject))
    }

    for (FieldConfigScheme resultPriorityScheme in resultPrioritySchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/ViewPrioritySchemes.jspa' >${resultPriorityScheme.name}</a></td>")
        html.append("<td>${prioritySchemeManager.getProjectsWithScheme(resultPriorityScheme)*.key}</td>")
        html.append("</tr>")
    }
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

List<Project> getFilteredProjects(String projectCategoryName) {
    return ComponentAccessor.projectManager.projects.stream()
            .filter ( p -> p?.projectCategory?.name.equals(projectCategoryName))
            .collect(Collectors.toList())
}
