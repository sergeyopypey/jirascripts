import com.atlassian.jira.scheme.Scheme
import com.atlassian.jira.scheme.SchemeEntity
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.permission.PermissionSchemeManager
import com.atlassian.jira.security.plugin.ProjectPermissionKey

final SCHEME_ID_1 = 11402 // First permission scheme ID
final SCHEME_ID_2 = 13000 // Second permission scheme ID
PermissionSchemeManager permissionSchemeManager = ComponentAccessor.getComponent(PermissionSchemeManager)

Scheme permissionScheme1 = permissionSchemeManager.getSchemeObject(SCHEME_ID_1)
Scheme permissionScheme2 = permissionSchemeManager.getSchemeObject(SCHEME_ID_2)

Collection<SchemeEntity> entities1 = permissionScheme1.getEntities()
Collection<SchemeEntity> entities2 = permissionScheme2.getEntities()

Set<ProjectPermissionKey> permissions = entities1*.entityTypeId.toSet() as Set<ProjectPermissionKey>

String html = new String("<table class='aui'>")
html += "<tr><th>Permission</th><th>$permissionScheme1.name</th><th>$permissionScheme2.name</th></tr>"
for (ProjectPermissionKey permission : permissions) {
    html += "<tr><th>${permission}</th>"
    log.warn(permission)
    Collection<SchemeEntity> entity1 = entities1.findAll { it.entityTypeId == permission }
    Collection<SchemeEntity> entity2 = entities2.findAll { it.entityTypeId == permission }

    String value1 = entity1.collect {
        it.getType() + ":" + it.getParameter()
    }.sort().join("<br>")
    String value2 = entity2.collect {
        it.getType() + ":" + it.getParameter()
    }.sort().join("<br>")

    if (value1.equals(value2)) {
        html += "<td>${value1}</td>"
        html += "<td>${value2}</td>"
    } else {
        html += "<td style='background-color: #ffcccc'>${value1}</td>"
        html += "<td style='background-color: #ffcccc'>${value2}</td>"
    }
    html += "</tr>"
}
html += "</table>"
return html
