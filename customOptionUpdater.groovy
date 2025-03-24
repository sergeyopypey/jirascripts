// Add a new option to a select list when an issue is created.

import com.atlassian.jira.issue.Issue
import org.apache.commons.lang3.StringUtils
import com.atlassian.jira.issue.fields.CustomField
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.customfields.option.Options
import com.atlassian.jira.issue.customfields.manager.OptionsManager

// Consts
final String SOURCE_TEXT_FIELD_CF_ID = "customfield_10500"
final String TARGET_SELECT_LIST_CF_ID = "customfield_10400"

// Classes
final CustomFieldManager customFieldManager = ComponentAccessor.getComponent(CustomFieldManager.class)
final OptionsManager optionsManageer = ComponentAccessor.getComponent(OptionsManager.class)

Issue issue = issue //Issues.getByKey("SOFTWARE-25")

CustomField sourceTextField = customFieldManager.getCustomFieldObject(SOURCE_TEXT_FIELD_CF_ID)
CustomField targetSelectListField = customFieldManager.getCustomFieldObject(TARGET_SELECT_LIST_CF_ID)

String sourceTextFieldValue = issue.getCustomFieldValue(sourceTextField)
if (StringUtils.isEmpty(sourceTextFieldValue)) {
    log.warn("Source Text Field is Empty")
    return
}

FieldConfig targetSelectListFieldConfig = targetSelectListField.getRelevantConfig(issue)
if (Objects.isNull(targetSelectListFieldConfig)) {
    log.warn("Target Field doesn't have context for current issue")
    return
}

Options targetSelectListFieldOptions = optionsManageer.getOptions(targetSelectListFieldConfig)
if (targetSelectListFieldOptions.any { it.value.equals(sourceTextFieldValue) }) {
    log.warn("Current text value already persist in Select List")
    return
}

try {
    targetSelectListFieldOptions.addOption(null, sourceTextFieldValue)
} catch (Exception ex) {
    log.error(ex)
}
