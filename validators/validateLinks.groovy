package validators

import webwork.action.ActionContext
import com.atlassian.jira.issue.Issue
import javax.servlet.http.HttpServletRequest
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.IssueFieldConstants
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.fields.IssueLinksSystemField

final String ALLOWED_LINK_TYPE = "is blocked by"
final Set<String> ALLOWED_ISSUE_TYPES = ["Bug", "Incident"]
final IssueManager issueManager = ComponentAccessor.getIssueManager()

HttpServletRequest httpServletRequest = ActionContext.getRequest()
String issueLinksLinkType = httpServletRequest.getParameter(IssueLinksSystemField.PARAMS_LINK_TYPE) ?:
        httpServletRequest.getParameterValues(IssueLinksSystemField.PARAMS_LINK_TYPE)?.getAt(0)
String[] issueLinkIssueKeys = httpServletRequest.getParameterValues(IssueLinksSystemField.PARAMS_ISSUE_KEYS)

if (Objects.isNull(issueLinkIssueKeys)) {
    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS, "You must link at least one valid blocking issue type $ALLOWED_ISSUE_TYPES")
}

log.warn("IssueLinksLinkType: " + issueLinksLinkType)
if (!ALLOWED_LINK_TYPE.equals(issueLinksLinkType)) {
    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS, "You must use '$ALLOWED_LINK_TYPE' link type")
}

boolean hasValidLinkIssues = issueLinkIssueKeys.any { issueLinkIssueKey ->
    Issue issueLinkIssue = issueManager.getIssueObject(issueLinkIssueKey)
    return ALLOWED_ISSUE_TYPES.contains(issueLinkIssue.issueType.name)
}

if (!hasValidLinkIssues) {
    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS, "You must link at least one valid issue type $ALLOWED_ISSUE_TYPES")
}
