import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.customfields.option.Option

final long SELECT_LIST_CF_ID = 10400

MutableIssue issue = (MutableIssue) issue
Issue originalIssue = (Issue) originalIssue

Option newValue = issue.getCustomFieldValue(SELECT_LIST_CF_ID)
Option oldValue = originalIssue.getCustomFieldValue(SELECT_LIST_CF_ID)

if (newValue.equals(oldValue)) {
    throw new InvalidInputException("customfield_" + SELECT_LIST_CF_ID, "Please change the field value to a different one")
}
