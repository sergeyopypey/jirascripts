import groovy.sql.Sql;
import java.sql.Connection;
import java.sql.SQLException;
import groovy.sql.GroovyRowResult;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.ConnectionFactory;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.jira.component.ComponentAccessor;

String query = """SELECT * FROM jiraissue"""
List<GroovyRowResult> result = dbSelect(query)
listToHtmlTable(result)

List<GroovyRowResult> dbSelect(String query) throws SQLException, GenericEntityException {
    OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
    DelegatorInterface delegatorInterface = delegator.getDelegatorInterface();
    String helperName = delegatorInterface.getGroupHelperName("default");

    try (Connection connection = ConnectionFactory.getConnection(helperName);
         Sql sql = new Sql(connection)) {

        return sql.rows(query);
    }
}

String listToHtmlTable(List<GroovyRowResult> dataList) {
    StringBuilder stringBuilder = new StringBuilder()
    
    stringBuilder.append("<table class='aui' border='1'>")

    // <-- Headers -->
    if (!dataList.isEmpty()) {
        stringBuilder.append("<thead>")
        stringBuilder.append("<tr>")

        GroovyRowResult firstRow = dataList.get(0)
        firstRow.keySet().each {key ->
            stringBuilder.append("<th>$key</th>")
        }

        stringBuilder.append("</tr>")
        stringBuilder.append("</thead>")
    }
    // <-- Headers -->

    // <-- Body -->
    stringBuilder.append("<tbody>")
    dataList.each { row ->
        stringBuilder.append("<tr>")
        row.each { key, value ->
            stringBuilder.append("<td>${value}</td>")
        }
        stringBuilder.append("</tr>")
    }
    stringBuilder.append("</tbody>")
    // <-- Body -->
    
    stringBuilder.append("</table>")

    return stringBuilder.toString()
}
