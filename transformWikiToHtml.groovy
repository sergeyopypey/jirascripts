import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.RendererManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin

Issue issue = Issues.getByKey("SOFTWARE-10")

transformWikiToHtml(issue, issue.description)

String transformWikiToHtml(Issue issue, String text) {
    RendererManager rendererManager = ComponentAccessor.rendererManager
    JiraRendererPlugin renderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
    return renderer.render(text, issue.issueRenderContext);
}
