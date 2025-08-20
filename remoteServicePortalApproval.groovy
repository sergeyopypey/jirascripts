package restendpoints

import groovy.json.JsonBuilder
import groovy.transform.BaseScript
import com.adaptavist.hapi.jira.users.Users
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.util.json.JSONObject
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate

import javax.ws.rs.core.Response
import javax.ws.rs.core.MultivaluedMap
import javax.servlet.http.HttpServletRequest

@BaseScript CustomEndpointDelegate delegate

final Set<String> allowedDecision = ["approve", "decline"]

approval(httpMethod: "POST", groups: ["jira-custom-approval"]) { MultivaluedMap queryParams, String body, HttpServletRequest request ->
    log.warn(body)
    try {
        JSONObject jsonObject = new JSONObject(body)
        String decision = jsonObject.getString("decision")
        String issueKey = jsonObject.getString("issueKey")
        String username = jsonObject.getString("username")

        if (!allowedDecision.contains(decision)) {
            log.error("Wrong 'decision'. Set 'approve' or 'decline' decision value")
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonBuilder([error: "Wrong 'decision'. Set 'approve' or 'decline' decision value"]).toString()).build()
        }

        final MutableIssue issueToApprove = ComponentAccessor.issueManager.getIssueByCurrentKey(issueKey)
        if (!issueToApprove) {
            log.error("Issue with key '$issueKey' doesn't exist")
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonBuilder([error: "Issue with key '$issueKey' doesn't exist"]).toString()).build()
        }
        
        final ApplicationUser approveUser = ComponentAccessor.userManager.getUserByName(username)
        if (Objects.isNull(approveUser)) {
            log.error("User with username '$username' doesn't exist")
            return Response.status(Response.Status.BAD_REQUEST).entity(new JsonBuilder([error: "User with username '$username' doesn't exist"]).toString()).build()
        }

        ServiceDesk.runAsCustomer {
            Users.runAs(approveUser, {
            if (decision.equalsIgnoreCase("approve")) {
                    issueToApprove.approve()
                } else if (decision.equalsIgnoreCase("decline")) {
                    issueToApprove.reject()
                }
            })
        }
        Users.runAs("jirabot", {
            issueToApprove.addComment("This request got $decision via ChatBot by $approveUser.displayName")
        })
        log.warn("Successful $decision for $issueKey by $username")
        return Response.ok(new JsonBuilder([success: "Successful $decision for $issueKey by $username"]).toString()).build()
    } catch (Exception exception) {
        log.error(exception)
        return Response.status(Response.Status.BAD_REQUEST).entity(new JsonBuilder([error: "$exception"]).toString()).build()
    }
}
