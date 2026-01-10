import groovy.transform.Field
import javax.ws.rs.core.Response
import groovy.transform.BaseScript
import java.util.stream.Collectors
import javax.ws.rs.core.MultivaluedMap
import com.atlassian.jira.project.Project
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager

@BaseScript CustomEndpointDelegate delegate

issueTypesScreenSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor.getComponent(IssueTypeScreenSchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Issue Type Screen Scheme</th><th>Screen Schemes</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")
    Set<IssueTypeScreenScheme> resultIssueTypeScreenSchemes = []

    for (Project filteredProject in filteredProjects) {
        resultIssueTypeScreenSchemes.add(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(filteredProject))
    }

    for (IssueTypeScreenScheme resultIssueTypeScreenScheme in resultIssueTypeScreenSchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/ConfigureIssueTypeScreenScheme.jspa?id=$resultIssueTypeScreenScheme.id' >${resultIssueTypeScreenScheme.name}</a></td>")
        html.append("<td>${issueTypeScreenSchemeManager.getIssueTypeScreenSchemeEntities(resultIssueTypeScreenScheme).collect { it.getFieldScreenScheme().getName() }}</td>")
        html.append("<td>${issueTypeScreenSchemeManager.getProjects(resultIssueTypeScreenScheme)*.originalkey}</td>")
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
