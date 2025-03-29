import webwork.action.ActionContext
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.IssueFieldConstants
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.fields.IssueLinksSystemField

final String ALLOWED_LINK_TYPE = "is blocked by"
final Set<String> ALLOWED_ISSUE_TYPES = ["Bug", "Incident"]
final IssueManager issueManager = ComponentAccessor.getIssueManager()

Issue issue = (Issue) issue

String issueLinksLinkType = ActionContext.request.getParameter(IssueLinksSystemField.PARAMS_LINK_TYPE)
String[] issueLinkIssueKeys = ActionContext.request.getParameterValues(IssueLinksSystemField.PARAMS_ISSUE_KEYS)

if (Objects.isNull(issueLinkIssueKeys)) {
    throw new InvalidInputException("You must link at least one blocking bug")
}

if (!ALLOWED_LINK_TYPE.equals(issueLinksLinkType)) {
    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS, "You must use 'is blocked by' link type")
}

boolean hasValidLinkIssues = issueLinkIssueKeys.any { issueLinkIssueKey ->
    Issue issueLinkIssue = issueManager.getIssueObject(issueLinkIssueKey)
    return ALLOWED_ISSUE_TYPES.contains(issueLinkIssue.issueType.name)
}

if (!hasValidLinkIssues) {
    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS, "You must link at least one valid issue type $ALLOWED_ISSUE_TYPES")
}
