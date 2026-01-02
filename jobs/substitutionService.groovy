import com.atlassian.jira.issue.Issue
import com.adaptavist.hapi.jira.users.Users
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.workflow.JiraWorkflow
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.loader.StepDescriptor
import com.atlassian.jira.event.type.EventDispatchOption
import com.opensymphony.workflow.loader.WorkflowDescriptor

// <-- Config -->
replaceApprovers("replacedUsername", "replacerUsername")
// <-- Config -->

void replaceApprovers(String replacedUsername, String replacerUsername) {
    final ApplicationUser replacedUser = Users.getByName(replacedUsername)
    final ApplicationUser replacerUser = Users.getByName(replacerUsername)

    if (replacedUser && replacerUser && replacerUser.isActive()) {
        Issues.search("Approvals = pendingBy(\"$replacedUsername\")").each { issue ->
            replaceApprovers(issue, replacedUser, replacerUser)
            sendNotification(issue, replacedUser, replacerUser)
        }
    }
}

void replaceAssignees(String replacedUsername, String replacerUsername) {
    final ApplicationUser replacedUser = Users.getByName(replacedUsername)
    final ApplicationUser replacerUser = Users.getByName(replacerUsername)
    final IssueManager issueManager = ComponentAccessor.getIssueManager()

    if (replacedUser && replacerUser && replacerUser.isActive()) {
        Issues.search("Assignee = $replacedUsername AND statusCategory != Done").each { issue ->
            MutableIssue mutableIssue = issueManager.getIssueByCurrentKey(issue.key)
            mutableIssue.setAssignee(replacerUser)
            issueManager.updateIssue(Users.loggedInUser, mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false)
            issue.addComment("Issue was re-assigned from inactive [~$replacedUser.username] to [~$replacerUser.username]")
        }
    }
}

void replaceApprovers(Issue issue, ApplicationUser replacedUser, ApplicationUser replacerUser) {
    CustomField approvalCustomField = getApprovalCustomField(issue)
    log.warn("approvalCustomField: $approvalCustomField")

    switch (approvalCustomField.customFieldType.key) {
        case "com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker":
            List<ApplicationUser> approvers = (issue.getCustomFieldValue(approvalCustomField) ?: []) as List<ApplicationUser>
            if (approvers.removeIf { it.username.equals(replacedUser.username) }) {
                approvers.add(replacerUser)
                log.warn("approvers: $approvers")
                issue.update {
                    setCustomFieldValue(approvalCustomField.getIdAsLong(), approvers)
                    setSendEmail(false)
                }
            }
            break
        case "com.atlassian.jira.plugin.system.customfieldtypes:userpicker":
            ApplicationUser approver = (issue.getCustomFieldValue(approvalCustomField) ?: null) as ApplicationUser
            if (approver.username.equals(replacedUser.username)) {
                issue.update {
                    setCustomFieldValue(approvalCustomField.getIdAsLong(), replacerUser)
                    setSendEmail(false)
                }
            }
            break
    }
}

CustomField getApprovalCustomField(Issue issue) {
    String approvalCustomFieldId = getApprovalCustomFieldId(issue)
    log.warn("approvalCustomFieldId: $approvalCustomFieldId")
    return ComponentAccessor.customFieldManager.getCustomFieldObject(approvalCustomFieldId)
}

String getApprovalCustomFieldId(Issue issue) {
    WorkflowDescriptor workflowDescriptor = getWorkflowDescriptorForIssue(issue)
    log.warn("workflowDescriptor: $workflowDescriptor")
    List<StepDescriptor> stepDescriptors = workflowDescriptor.getSteps()
    log.warn("stepDescriptors: $stepDescriptors")

    StepDescriptor stepDescriptor = stepDescriptors.find { step -> step.name.equals(issue.status.name) }
    log.warn("stepDescriptor: $stepDescriptor")

    return stepDescriptor.getMetaAttributes().get("approval.field.id")
}

WorkflowDescriptor getWorkflowDescriptorForIssue(Issue issue) {
    JiraWorkflow jiraWorkflow = ComponentAccessor.workflowManager.getWorkflow(issue)
    log.warn("jiraWorkflow: $jiraWorkflow")
    return jiraWorkflow.getDescriptor()
}

void sendNotification(Issue issue, ApplicationUser replacedUser, ApplicationUser replacerUser) {
    //notification holder
}
