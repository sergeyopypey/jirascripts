/*
 * Batch rescue / filter-reassign for Jira Agile (Greenhopper) boards.
 *
 * Purpose:
 *   Reassign a list of RapidBoards to a given saved filter, including boards
 *   whose current filter has been deleted (orphaned boards).
 *
 * Why not RapidViewService?
 *   The public RapidViewService validates the board's *current* savedFilterId
 *   on both getRapidView() and update(). If the filter no longer exists, both
 *   calls return NOT_FOUND (gh.rapid.view.error.noview) and the board is
 *   unrecoverable via the public API.
 *
 *   The internal RapidViewManager is admin-level and skips this validation,
 *   so it can read and rewrite orphaned boards.
 *
 * Input:
 *   boardIds     - List<Long> of RapidView IDs to process
 *   newFilterId  - Long, the target saved filter ID (must exist)
 *
 * Output:
 *   Per-board status line: rescued / skipped (already correct) / failed.
 */

import com.atlassian.greenhopper.service.ServiceResult
import com.atlassian.greenhopper.model.rapid.RapidView
import com.atlassian.greenhopper.service.ServiceOutcome
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import com.onresolve.scriptrunner.runner.customisers.JiraAgileBean
import com.atlassian.greenhopper.manager.rapidview.RapidViewManager

@WithPlugin("com.pyxis.greenhopper.jira")
@JiraAgileBean RapidViewManager rapidViewManager

// ---- configure ----
final List<Long> boardIds = [5L]
final Long newFilterId = 10200L
// -------------------

List<String> lines = []

for (Long boardId : boardIds) {
    ServiceOutcome<RapidView> getOutcome = rapidViewManager.get(boardId)
    if (!getOutcome.valid) {
        lines << "id=${boardId}: LOOKUP FAILED — ${getOutcome.errors}".toString()
        continue
    }

    RapidView rv = getOutcome.value
    Long before = rv.savedFilterId

    if (before == newFilterId) {
        lines << "id=${boardId} '${rv.name}': already uses filter ${newFilterId} — skipped".toString()
        continue
    }

    RapidView fixed = RapidView.builder(rv).savedFilterId(newFilterId).build()
    ServiceResult updateResult = rapidViewManager.update(fixed)
    if (!updateResult.valid) {
        lines << "id=${boardId} '${rv.name}': UPDATE FAILED — ${updateResult.errors}".toString()
        continue
    }

    lines << "id=${boardId} '${rv.name}': ${before} -> ${newFilterId}".toString()
}

return lines.join("\n")
