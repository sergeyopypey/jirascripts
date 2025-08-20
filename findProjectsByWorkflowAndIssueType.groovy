import com.atlassian.jira.project.Project
import com.atlassian.jira.workflow.JiraWorkflow
import com.atlassian.jira.workflow.WorkflowScheme
import com.atlassian.jira.workflow.WorkflowManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.WorkflowSchemeManager
import com.atlassian.jira.workflow.AssignableWorkflowScheme

final String ISSUE_TYPE_ID = "10100"
final String WORKFLOW_NAME = "New Feature Workflow"

WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager()
WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager()

JiraWorkflow jiraWorkflow = workflowManager.getWorkflow(WORKFLOW_NAME)

Iterable<WorkflowScheme> workflowSchemes = workflowSchemeManager.getSchemesForWorkflowIncludingDrafts(jiraWorkflow)

List<Project> resultingProjects = new ArrayList<>()
for (WorkflowScheme workflowScheme in workflowSchemes) {
    Map<String, String> mappings = workflowScheme.getMappings()
    boolean doesFeatureReallyUsesThisWorkflow = mappings.get(ISSUE_TYPE_ID).equals(WORKFLOW_NAME)
    if (doesFeatureReallyUsesThisWorkflow) {
        List<Project> projectsUsingThisScheme = workflowSchemeManager.getProjectsUsing(workflowScheme as AssignableWorkflowScheme)
        log.warn(projectsUsingThisScheme)
        resultingProjects.addAll(projectsUsingThisScheme)
    }
}

return resultingProjects
