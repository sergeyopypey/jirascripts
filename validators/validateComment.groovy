package validators

import webwork.action.ActionContext
import com.atlassian.jira.issue.Issue
import org.apache.commons.lang3.StringUtils
import com.atlassian.jira.issue.IssueFieldConstants
import com.opensymphony.workflow.InvalidInputException

Issue issue = (Issue) issue

String resolutionName = issue.resolution?.name

if (resolutionName.equals("Rejected")) {
    String comment = ActionContext.request.getParameter(IssueFieldConstants.COMMENT)

    if (StringUtils.isBlank(comment)) {
        throw new InvalidInputException(IssueFieldConstants.COMMENT, "Please specify comment")
    }
}
