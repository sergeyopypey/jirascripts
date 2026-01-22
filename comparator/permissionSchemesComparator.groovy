import com.atlassian.jira.scheme.Scheme
import com.atlassian.jira.scheme.SchemeEntity
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.permission.PermissionSchemeManager
import com.atlassian.jira.security.plugin.ProjectPermissionKey

final SCHEME_ID_1 = 10700
final SCHEME_ID_2 = 10402
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

    Collection<String> values1 = entity1.collect {
        it.getType() + ":" + it.getParameter()
    }
    Collection<String> values2 = entity2.collect {
        it.getType() + ":" + it.getParameter()
    }
    
    html += "<td>"
    html += values1.collect { values2.contains(it) ? "<span style='color:green;'>$it</span>" : "<span style='color:red;'>$it</span>" }.join("<br>")
    html += "</td>"
    html += "<td>"
    html += values2.collect { values1.contains(it) ? "<span style='color:green;'>$it</span>" : "<span style='color:red;'>$it</span>" }.join("<br>")
    html += "</td>"
    html += "</tr>"
}
html += "</table>"
return html
