import javax.ws.rs.core.Response
import groovy.transform.BaseScript
import java.util.stream.Collectors
import javax.ws.rs.core.MultivaluedMap
import com.atlassian.jira.project.Project
import com.atlassian.jira.config.IssueTypeManager
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager

@BaseScript CustomEndpointDelegate delegate

issueTypesScreenStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    StringBuilder html = new StringBuilder()
    html.append("<table style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\">")
    List<Project> filteredProjects = getFilteredProjects("Agile CoE")

    addIssueTypeStatsToTable(filteredProjects, "10004", html)
    addIssueTypeStatsToTable(filteredProjects, "11904", html)
    addIssueTypeStatsToTable(filteredProjects, "13431", html)
    addIssueTypeStatsToTable(filteredProjects, "13432", html)
    html.append("<tr><th>Issue Type</th><th>Screen Schemes</th><th>Screens</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "12300", html)
    addIssueTypeStatsToTable(filteredProjects, "13428", html)
    addIssueTypeStatsToTable(filteredProjects, "11900", html)
    addIssueTypeStatsToTable(filteredProjects, "13437", html)
    addIssueTypeStatsToTable(filteredProjects, "11300", html)
    addIssueTypeStatsToTable(filteredProjects, "10100", html)
    addIssueTypeStatsToTable(filteredProjects, "13420", html)
    addIssueTypeStatsToTable(filteredProjects, "10001", html)
    addIssueTypeStatsToTable(filteredProjects, "13411", html)
    addIssueTypeStatsToTable(filteredProjects, "11501", html)
    addIssueTypeStatsToTable(filteredProjects, "13417", html)
    addIssueTypeStatsToTable(filteredProjects, "14001", html)
    addIssueTypeStatsToTable(filteredProjects, "15100", html)
    addIssueTypeStatsToTable(filteredProjects, "15900", html)
    addIssueTypeStatsToTable(filteredProjects, "16100", html)
    html.append("<tr><th>Issue Type</th><th>Screen Schemes</th><th>Screens</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "14101", html)
    addIssueTypeStatsToTable(filteredProjects, "14100", html)
    addIssueTypeStatsToTable(filteredProjects, "14202", html)
    addIssueTypeStatsToTable(filteredProjects, "13901", html)
    addIssueTypeStatsToTable(filteredProjects, "15700", html)
    addIssueTypeStatsToTable(filteredProjects, "14901", html)
    addIssueTypeStatsToTable(filteredProjects, "14900", html)
    addIssueTypeStatsToTable(filteredProjects, "13900", html)
    addIssueTypeStatsToTable(filteredProjects, "14201", html)
    html.append("<tr><th>Issue Type</th><th>Screen Schemes</th><th>Screens</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "10607", html)
    addIssueTypeStatsToTable(filteredProjects, "12004", html)
    addIssueTypeStatsToTable(filteredProjects, "11903", html)
    addIssueTypeStatsToTable(filteredProjects, "15300", html)
    addIssueTypeStatsToTable(filteredProjects, "15200", html)
    addIssueTypeStatsToTable(filteredProjects, "15500", html)
    addIssueTypeStatsToTable(filteredProjects, "15403", html)
    addIssueTypeStatsToTable(filteredProjects, "12301", html)
    addIssueTypeStatsToTable(filteredProjects, "13433", html)

    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

void addIssueTypeStatsToTable(List<Project> filteredProjects, String issueTypeId, StringBuilder html) {
    final String SCREEN_URL   = "https://jira.local/secure/admin/ConfigureFieldScreen.jspa?id=%d"
    final String SCREEN_SCHEME_URL = "https://jira.local/secure/admin/ConfigureFieldScreenScheme.jspa?id=%d"
    final IssueTypeManager issueTypeManager = ComponentAccessor.getComponent(IssueTypeManager.class)
    final IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getComponent(IssueTypeSchemeManager.class)
    final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor.getComponent(IssueTypeScreenSchemeManager.class)
    
    List<FieldScreen> resultFieldScreen = new ArrayList<>()
    List<FieldScreenScheme> resultFieldScreenScheme = new ArrayList<>()
    List<FieldScreenSchemeItem> resultFieldScreenSchemeItems = new ArrayList<>()

    for (Project filteredProject in filteredProjects) {
        Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(filteredProject)
        boolean doesContainIssueType = issueTypes.any { it.id.equals(issueTypeId) }
        if (doesContainIssueType) {
            IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(filteredProject)
            IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(issueTypeId)
            if (issueTypeScreenSchemeEntity == null) {
                issueTypeScreenSchemeEntity = issueTypeScreenScheme.getEntity(null)
            }

            FieldScreenScheme fieldScreenScheme = issueTypeScreenSchemeEntity.getFieldScreenScheme()
            resultFieldScreenScheme.add(fieldScreenScheme)
            List<FieldScreenSchemeItem> fieldScreenSchemeItems = fieldScreenScheme.getFieldScreenSchemeItems() as List<FieldScreenSchemeItem>
            resultFieldScreenSchemeItems.addAll(fieldScreenSchemeItems)
            List<FieldScreen> fieldScreens = fieldScreenSchemeItems.collect { it.fieldScreen }.flatten() as List<FieldScreen>
            resultFieldScreen.addAll(fieldScreens)
        }
    }


    resultFieldScreen.unique { it.id }. eachWithIndex{ FieldScreen entry, int i ->
        html.append("<tr>")
        if (i == 0) {
            html.append("<td style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\" rowspan='${resultFieldScreen.size()}'>${issueTypeManager.getIssueType(issueTypeId).name}</td>")
            html.append("<td style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\" rowspan='${resultFieldScreen.size()}'>")
            resultFieldScreenScheme.unique {it.id }.each { html.append("<br><a href='${String.format(SCREEN_SCHEME_URL, it.id)}'>$it.name</br>")}
            html.append("</td>")
        }
        html.append("<td style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\"><a href='${String.format(SCREEN_URL, entry.id)}'>$entry.name</a></td>")
        html.append("</tr>")
    }
}

List<Project> getFilteredProjects(String projectCategoryName) {
    return ComponentAccessor.projectManager.projects.stream()
            .filter ( p -> p?.projectCategory?.name.equals(projectCategoryName))
            .collect(Collectors.toList())
}
