import org.apache.commons.text.StringEscapeUtils
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.search.SearchRequestEntity
import com.atlassian.jira.issue.search.SearchRequestManager

final List<String> requiredKeywords = ["Unresolved", "resolution"]
final String baseUrl = ComponentAccessor.applicationProperties.getJiraBaseUrl()
final SearchRequestManager searchRequestManager = ComponentAccessor.getComponent(SearchRequestManager)

StringBuilder html = new StringBuilder()
html.with {
    append("<table class='aui'>")
    append("<tr><th>Filter Name</th><th>JQL</th></tr>")
    searchRequestManager.visitAll { SearchRequestEntity searchRequestEntity ->
        String jqlBody = searchRequestEntity.getRequest()

        if (jqlBody && requiredKeywords.every { kw -> jqlBody.contains(kw) }) {
            String escapedJql = StringEscapeUtils.escapeHtml4(jqlBody)
            String filterUrl = "${baseUrl}/issues/?filter=${searchRequestEntity.id}"
            String filterName = StringEscapeUtils.escapeHtml4(searchRequestEntity.name ?: "Unnamed filter")

            append("<tr>")
            append("<td><a href='${filterUrl}' target='_blank'>${filterName}</a></td>")
            append("<td><code>${escapedJql}</code></td>")
            append("</tr>")
        }
    }

    append("</table>")
}

return html.toString()
