import groovy.transform.BaseScript
import java.util.stream.Collectors
import com.atlassian.jira.project.Project
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.WorkflowSchemeManager
import com.atlassian.jira.workflow.AssignableWorkflowScheme
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

workflowSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getComponent(WorkflowSchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Workflow Scheme</th><th>Workflows</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")
    Set<AssignableWorkflowScheme> resultWorkflowSchemes = []

    log.warn(filteredProjects.size())
    for (Project filteredProject in filteredProjects) {
        AssignableWorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(filteredProject)
        resultWorkflowSchemes.add(workflowScheme)
    }

    for (resultWorkflowScheme in resultWorkflowSchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.example.com/secure/admin/EditWorkflowScheme.jspa?schemeId=$resultWorkflowScheme.id' >${resultWorkflowScheme.name}</a></td>")
        html.append("<td>${resultWorkflowScheme.getMappings().values()}</td>")
        html.append("<td>${workflowSchemeManager.getProjectsUsing(resultWorkflowScheme).collect {it.key} }</td>")
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
