import groovy.transform.Field
import javax.ws.rs.core.Response
import groovy.transform.BaseScript
import java.util.stream.Collectors
import javax.ws.rs.core.MultivaluedMap
import com.atlassian.jira.project.Project
import com.atlassian.jira.config.IssueTypeManager
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.WorkflowSchemeManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager

@BaseScript CustomEndpointDelegate delegate

issueTypesWorkflowStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    StringBuilder html = new StringBuilder()
    html.append("<table style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\">")
    html.append("<tr><th>Issue Type</th><th>Workflows</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")

    addIssueTypeStatsToTable(filteredProjects, "10100", html)
    addIssueTypeStatsToTable(filteredProjects, "13420", html)
    addIssueTypeStatsToTable(filteredProjects, "10001", html)
    addIssueTypeStatsToTable(filteredProjects, "13411", html)
    addIssueTypeStatsToTable(filteredProjects, "10607", html)
    addIssueTypeStatsToTable(filteredProjects, "13434", html)
    addIssueTypeStatsToTable(filteredProjects, "12301", html)
    addIssueTypeStatsToTable(filteredProjects, "13433", html)
    addIssueTypeStatsToTable(filteredProjects, "10004", html)
    addIssueTypeStatsToTable(filteredProjects, "13431", html)
    addIssueTypeStatsToTable(filteredProjects, "11200", html)
    addIssueTypeStatsToTable(filteredProjects, "13416", html)
    addIssueTypeStatsToTable(filteredProjects, "13700", html)
    addIssueTypeStatsToTable(filteredProjects, "12100", html)
    addIssueTypeStatsToTable(filteredProjects, "13423", html)
    html.append("<tr><th>Issue Type</th><th>Workflows</th><th>Projects</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "10609", html)
    addIssueTypeStatsToTable(filteredProjects, "13413", html)
    addIssueTypeStatsToTable(filteredProjects, "12002", html)
    addIssueTypeStatsToTable(filteredProjects, "13435", html)
    addIssueTypeStatsToTable(filteredProjects, "12001", html)
    addIssueTypeStatsToTable(filteredProjects, "13436", html)
    html.append("<tr><th>Issue Type</th><th>Workflows</th><th>Projects</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "16100", html)
    addIssueTypeStatsToTable(filteredProjects, "15100", html)
    addIssueTypeStatsToTable(filteredProjects, "15900", html)
    addIssueTypeStatsToTable(filteredProjects, "15101", html)
    addIssueTypeStatsToTable(filteredProjects, "14801", html)
    addIssueTypeStatsToTable(filteredProjects, "14802", html)
    addIssueTypeStatsToTable(filteredProjects, "12300", html)
    addIssueTypeStatsToTable(filteredProjects, "13428", html)
    addIssueTypeStatsToTable(filteredProjects, "11900", html)
    addIssueTypeStatsToTable(filteredProjects, "13437", html)
    addIssueTypeStatsToTable(filteredProjects, "10000", html)
    addIssueTypeStatsToTable(filteredProjects, "12004", html)
    addIssueTypeStatsToTable(filteredProjects, "11903", html)
    addIssueTypeStatsToTable(filteredProjects, "13424", html)
    addIssueTypeStatsToTable(filteredProjects, "15300", html)
    addIssueTypeStatsToTable(filteredProjects, "15200", html)
    addIssueTypeStatsToTable(filteredProjects, "15500", html)
    addIssueTypeStatsToTable(filteredProjects, "15403", html)
    addIssueTypeStatsToTable(filteredProjects, "11501", html)
    addIssueTypeStatsToTable(filteredProjects, "13417", html)
    addIssueTypeStatsToTable(filteredProjects, "14001", html)
    addIssueTypeStatsToTable(filteredProjects, "11300", html)
    addIssueTypeStatsToTable(filteredProjects, "13429", html)
    addIssueTypeStatsToTable(filteredProjects, "14400", html)
    html.append("<tr><th>Issue Type</th><th>Workflows</th><th>Projects</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "14101", html)
    addIssueTypeStatsToTable(filteredProjects, "14100", html)
    addIssueTypeStatsToTable(filteredProjects, "14202", html)
    addIssueTypeStatsToTable(filteredProjects, "13901", html)
    addIssueTypeStatsToTable(filteredProjects, "15700", html)
    addIssueTypeStatsToTable(filteredProjects, "14901", html)
    addIssueTypeStatsToTable(filteredProjects, "14900", html)
    addIssueTypeStatsToTable(filteredProjects, "13900", html)
    addIssueTypeStatsToTable(filteredProjects, "14201", html)

    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

void addIssueTypeStatsToTable(List<Project> filteredProjects, String issueTypeId, StringBuilder html) {
    IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class)
    IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getComponent(IssueTypeSchemeManager.class)
    WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getComponent(WorkflowSchemeManager.class)

    Set<String> resultingWorkflowNames = new HashSet<>()

    for (Project filteredProject in filteredProjects) {
        Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(filteredProject)
        boolean doesContainIssueType = issueTypes.any { it.id.equals(issueTypeId) }
        if (doesContainIssueType) {
            Map<String, String> workflowMap = workflowSchemeManager.getWorkflowMap(filteredProject)
            String workflowName = workflowMap.get(issueTypeId)
            if (workflowName == null) workflowName = workflowMap.get(null)

            if (workflowName && !workflowName.contains("TESTCC")) {
                resultingWorkflowNames.add(workflowName)
            }
        }
    }

    resultingWorkflowNames.sort().eachWithIndex{ String entry, int i ->
        html.append("<tr>")
        if (i == 0) {
            html.append("<td style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\" rowspan='${resultingWorkflowNames.size()}'>${issueTypeManager.getIssueType(issueTypeId).name}</td>")
        }
        html.append("<td style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\"><a href=''>$entry</a></td>")
        html.append("<td style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\">$entry</td>")
        html.append("</tr>")
    }
}

List<Project> getFilteredProjects(String projectCategoryName) {
    return ComponentAccessor.projectManager.projects.stream()
            .filter ( p -> p?.projectCategory?.name.equals(projectCategoryName))
            .collect(Collectors.toList())
}
