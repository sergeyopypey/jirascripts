import javax.ws.rs.core.Response
import javax.ws.rs.core.MultivaluedMap

import groovy.transform.BaseScript
import java.util.stream.Collectors
import com.atlassian.jira.scheme.Scheme
import com.atlassian.jira.project.Project
import com.atlassian.jira.config.IssueTypeManager
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.WorkflowSchemeManager
import com.atlassian.jira.workflow.AssignableWorkflowScheme
import com.atlassian.jira.permission.PermissionSchemeManager
import com.atlassian.jira.issue.fields.screen.FieldScreen
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem
import com.atlassian.jira.issue.fields.config.FieldConfigScheme
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager
import com.atlassian.jira.issue.fields.config.manager.PrioritySchemeManager
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager

@BaseScript CustomEndpointDelegate delegate

workflowSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    final String projectCategoryName = queryParams.containsKey("projectCategoryName") ? queryParams.getFirst("projectCategoryName") : null
    final WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getComponent(WorkflowSchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Workflow Scheme</th><th>Workflows</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects(projectCategoryName)
    Set<AssignableWorkflowScheme> resultWorkflowSchemes = []

    for (Project filteredProject in filteredProjects) {
        AssignableWorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(filteredProject)
        resultWorkflowSchemes.add(workflowScheme)
    }

    for (resultWorkflowScheme in resultWorkflowSchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/EditWorkflowScheme.jspa?schemeId=$resultWorkflowScheme.id' >${resultWorkflowScheme.name}</a></td>")
        html.append("<td>${resultWorkflowScheme.getMappings().values()}</td>")
        html.append("<td>${filterProjects(projectCategoryName, workflowSchemeManager.getProjectsUsing(resultWorkflowScheme))*.key}</td>")
        html.append("</tr>")
    }
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

issueTypeSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    final String projectCategoryName = queryParams.containsKey("projectCategoryName") ? queryParams.getFirst("projectCategoryName") : null
    IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getComponent(IssueTypeSchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table>")
    html.append("<tr><th>Issue Type Scheme</th><th>Issue types</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects(projectCategoryName)
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
        html.append("<td>${filterProjects(projectCategoryName, resultFieldConfigScheme.associatedProjectObjects)*.key}</td>")
        html.append("</tr>")
    }
    log.warn(resultFilteredProjects.size())
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

issueTypesScreenStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    final String projectCategoryName = queryParams.containsKey("projectCategoryName") ? queryParams.getFirst("projectCategoryName") : null
    StringBuilder html = new StringBuilder()
    html.append("<table style=\"border: 1px solid black; border-collapse: collapse; padding: 5px\">")
    List<Project> filteredProjects = getFilteredProjects(projectCategoryName)

    html.append("<tr><th>Issue Type</th><th>Screen Schemes</th><th>Screens</th></tr>")
    addIssueTypeStatsToTable(filteredProjects, "10000", html)
    addIssueTypeStatsToTable(filteredProjects, "10001", html)
    addIssueTypeStatsToTable(filteredProjects, "10002", html)
    addIssueTypeStatsToTable(filteredProjects, "10003", html)
    addIssueTypeStatsToTable(filteredProjects, "10004", html)
    addIssueTypeStatsToTable(filteredProjects, "10005", html)
    addIssueTypeStatsToTable(filteredProjects, "10007", html)
    addIssueTypeStatsToTable(filteredProjects, "10202", html)
    addIssueTypeStatsToTable(filteredProjects, "10600", html)
    addIssueTypeStatsToTable(filteredProjects, "11513", html)
    addIssueTypeStatsToTable(filteredProjects, "11404", html)
    addIssueTypeStatsToTable(filteredProjects, "10010", html)
    addIssueTypeStatsToTable(filteredProjects, "11514", html)
    

    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

permissionSchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    final String projectCategoryName = queryParams.containsKey("projectCategoryName") ? queryParams.getFirst("projectCategoryName") : null
    PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getPermissionSchemeManager()

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Permission Scheme</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects(projectCategoryName)
    Set<Scheme> resultPermissionSchemes = []

    for (Project filteredProject in filteredProjects) {
        resultPermissionSchemes.add(permissionSchemeManager.getSchemeFor(filteredProject))
    }

    for (Scheme resultPermissionScheme in resultPermissionSchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/EditPermissions!default.jspa?schemeId=$resultPermissionScheme.id' >${resultPermissionScheme.name}</a></td>")
        html.append("<td>${filterProjects(projectCategoryName, permissionSchemeManager.getProjects(resultPermissionScheme))*.key}</td>")
        html.append("</tr>")
    }
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

prioritySchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    final String projectCategoryName = queryParams.containsKey("projectCategoryName") ? queryParams.getFirst("projectCategoryName") : null
    PrioritySchemeManager prioritySchemeManager = ComponentAccessor.getComponent(PrioritySchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Priority Scheme</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects(projectCategoryName)
    Set<FieldConfigScheme> resultPrioritySchemes = []

    for (Project filteredProject in filteredProjects) {
        resultPrioritySchemes.add(prioritySchemeManager.getScheme(filteredProject))
    }

    for (FieldConfigScheme resultPriorityScheme in resultPrioritySchemes.sort()) {
        html.append("<tr>")
        html.append("<td><a href='https://jira.local/secure/admin/ViewPrioritySchemes.jspa' >${resultPriorityScheme.name}</a></td>")
        html.append("<td>${filterProjects(projectCategoryName, prioritySchemeManager.getProjectsWithScheme(resultPriorityScheme).toList())*.key}</td>")
        html.append("</tr>")
    }
    html.append("</table>")

    return Response.ok(html.toString(), "text/html").build()
}

issueSecuritySchemeStats(httpMethod: "GET") { MultivaluedMap queryParams, String body ->
    final String projectCategoryName = queryParams.containsKey("projectCategoryName") ? queryParams.getFirst("projectCategoryName") : null
    IssueSecuritySchemeManager issueSecuritySchemeManager = ComponentAccessor.getComponent(IssueSecuritySchemeManager.class)

    StringBuilder html = new StringBuilder()
    html.append("<table class='aui'>")
    html.append("<tr><th>Security Scheme</th><th>Projects</th></tr>")
    List<Project> filteredProjects = getFilteredProjects(projectCategoryName)
    Set<Scheme> resultIssueSecuritySchemes = []

    for (Project filteredProject in filteredProjects) {
        resultIssueSecuritySchemes.add(issueSecuritySchemeManager.getSchemeFor(filteredProject))
    }

    for (Scheme resultIssueSecurityScheme in resultIssueSecuritySchemes.sort()) {
        if (resultIssueSecurityScheme) {
            html.append("<tr>")
            html.append("<td><a href='https://jira.local/secure/admin/EditPermissions!default.jspa?schemeId=$resultIssueSecurityScheme.id' >${resultIssueSecurityScheme.name}</a></td>")
            html.append("<td>${filterProjects(projectCategoryName, issueSecuritySchemeManager.getProjects(resultIssueSecurityScheme))*.key}</td>")
            html.append("</tr>")
        }
    }
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

List<Project> filterProjects(String projectCategoryName, List<Project> projects) {
    return projects.stream()
            .filter ( p -> p?.projectCategory?.name.equals(projectCategoryName))
            .collect(Collectors.toList())
}