package validators

import com.google.common.io.Files
import webwork.action.ActionContext
import com.atlassian.jira.issue.IssueFieldConstants
import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.fields.AttachmentSystemField
import com.atlassian.jira.issue.attachment.TemporaryWebAttachment
import com.atlassian.jira.issue.attachment.TemporaryWebAttachmentManager

final Set<String> ALLOWED_EXTENSION = ["pdf", "docx"]
final TemporaryWebAttachmentManager temporaryWebAttachmentManager = ComponentAccessor.getComponent(TemporaryWebAttachmentManager.class)

String[] temporaryWebAttachmentStringIds = ActionContext.request.getParameterValues(AttachmentSystemField.FILETOCONVERT)

if (Objects.isNull(temporaryWebAttachmentStringIds)) {
    log.warn("No attachments were added")
    throw new InvalidInputException(IssueFieldConstants.ATTACHMENT, "You must attach at least one file with one of the following extensions: $ALLOWED_EXTENSION")
}

Collection<TemporaryWebAttachment> temporaryWebAttachments = temporaryWebAttachmentStringIds.collect {tempAttachmentStringId ->
    temporaryWebAttachmentManager.getTemporaryWebAttachment(tempAttachmentStringId).getOrNull()
}

boolean hasValidAttachment = temporaryWebAttachments.any {temporaryWebAttachment ->
    String fileExtension = Files.getFileExtension(temporaryWebAttachment.filename).toLowerCase()
    return ALLOWED_EXTENSION.contains(fileExtension)
}

if (!hasValidAttachment) {
    throw new InvalidInputException(IssueFieldConstants.ATTACHMENT, "You must attach at least one file with one of the following extensions: $ALLOWED_EXTENSION")
}
