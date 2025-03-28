import groovy.transform.Field
import org.ofbiz.core.entity.GenericValue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.ofbiz.OfBizDelegator
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.ErrorCollection
import com.atlassian.jira.issue.fields.CustomField
import com.opensymphony.workflow.spi.WorkflowEntry
import com.atlassian.jira.workflow.TransitionOptions
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.workflow.IssueWorkflowManager
import com.atlassian.jira.workflow.WorkflowTransitionUtil
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.workflow.WorkflowTransitionUtilFactory

@Field final IssueWorkflowManager issueWorkflowManager = ComponentAccessor.getComponent(IssueWorkflowManager.class)
@Field final WorkflowTransitionUtilFactory workflowTransitionUtilFactory = ComponentAccessor.getComponent(WorkflowTransitionUtilFactory.class)
@Field final long SELECT_LIST_CF_ID = 10400

@Field final Map<String, Integer> OPTIONS_AND_TRANSITIONS_MAP = [
    "Option #1": 11,
    "Option #2": 21,
    "CustomOption #3": 31

]

MutableIssue issue = (MutableIssue) issue

CustomField optionField = ComponentAccessor.customFieldManager.getCustomFieldObject(SELECT_LIST_CF_ID)
Option option = issue.getCustomFieldValue(optionField) as Option

if (Objects.isNull(option)) {
    log.warn("Option field is empty")
    return
}

String optionValue = option.getValue()
Integer transitionId = OPTIONS_AND_TRANSITIONS_MAP.get(optionValue)

if (Objects.isNull(transitionId)) {
    log.warn("No mapping for $optionValue option")
    return
}

doTransition(issue, transitionId, Users.getByName("sergeyopypey"), true)

boolean doTransition(MutableIssue mutableIssue, int actionId, ApplicationUser user, boolean skipRestrictions) {
    final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator()
    final IssueWorkflowManager issueWorkflowManager = ComponentAccessor.getComponent(IssueWorkflowManager.class)
    final WorkflowTransitionUtilFactory workflowTransitionUtilFactory = ComponentAccessor.getComponent(WorkflowTransitionUtilFactory.class)

    GenericValue gv = ofBizDelegator.findByPrimaryKey("OSWorkflowEntry", mutableIssue.getWorkflowId());
    if (Objects.isNull(gv)) {
        log.warn("gv is null")
        return
    }

    if ((Integer) gv.get("state") == WorkflowEntry.CREATED) {
        gv.set("state", WorkflowEntry.ACTIVATED)
        gv.store()
    }

    TransitionOptions.Builder builder = new TransitionOptions.Builder();
    TransitionOptions transitionOptions =
        skipRestrictions
            ? builder.skipConditions().skipPermissions().skipValidators().build()
            : builder.build()

    if (!issueWorkflowManager.isValidAction(mutableIssue, actionId, transitionOptions, user)) {
        return false
    }

    WorkflowTransitionUtil workflowTransitionUtil = workflowTransitionUtilFactory.create()
    workflowTransitionUtil.setAction(actionId)
    workflowTransitionUtil.setIssue(mutableIssue)
    workflowTransitionUtil.setUserkey(user.getKey())

    Map<String, ErrorCollection> errors = new HashMap<>();
    errors.put("validate", workflowTransitionUtil.validate());
    boolean result = !errors.get("validate").hasAnyErrors();
    errors.put("progress", workflowTransitionUtil.progress());

    if (errors.get("progress").hasAnyErrors()) {
        result = false;
    }

    return result;
}