-- PostgreSQL 16
SELECT ss.*,
       CASE WHEN ss."Last Update" < CURRENT_DATE - INTERVAL '12 months' THEN 'NO' ELSE 'YES' END AS "Updated in the last 12 months?",
       cu2.lower_user_name AS "Last Update Author",
       COALESCE(
         (SELECT cu3.lower_user_name
          FROM jiraissue ji2
          LEFT JOIN app_user au3 ON ji2.assignee = au3.user_key
          LEFT JOIN cwd_user cu3 ON au3.lower_user_name = cu3.lower_user_name
          WHERE ji2.project = ss.id
            AND cu3.active = 1
            AND ji2.assignee IS NOT NULL
          LIMIT 1),
         (SELECT cu3.lower_user_name
          FROM jiraissue ji2
          LEFT JOIN app_user au3 ON ji2.reporter = au3.user_key
          LEFT JOIN cwd_user cu3 ON au3.lower_user_name = cu3.lower_user_name
          WHERE ji2.project = ss.id
            AND cu3.active = 1
            AND ji2.reporter IS NOT NULL
          LIMIT 1)
       ) AS "First Active Assignee or Reporter",
       COALESCE(
         (SELECT cu3.lower_email_address
          FROM jiraissue ji2
          LEFT JOIN app_user au3 ON ji2.assignee = au3.user_key
          LEFT JOIN cwd_user cu3 ON au3.lower_user_name = cu3.lower_user_name
          WHERE ji2.project = ss.id
            AND cu3.active = 1
            AND ji2.assignee IS NOT NULL
          LIMIT 1),
         (SELECT cu3.lower_email_address
          FROM jiraissue ji2
          LEFT JOIN app_user au3 ON ji2.reporter = au3.user_key
          LEFT JOIN cwd_user cu3 ON au3.lower_user_name = cu3.lower_user_name
          WHERE ji2.project = ss.id
            AND cu3.active = 1
            AND ji2.reporter IS NOT NULL
          LIMIT 1)
       ) AS "First Active Assignee or Reporter Email"
FROM (
  SELECT p.pkey AS "Project Key",
         p.id,
         CASE WHEN p.pkey = p.originalkey THEN NULL ELSE p.originalkey END AS "Original Key",
         CASE WHEN p.id = pe.entity_id THEN 'YES' ELSE 'NO' END AS "Project Archived?",
         cu.lower_user_name AS "Project Lead",
         cu.lower_email_address AS "Project Lead Email",
         CASE WHEN cu.active = 1 THEN 'YES' ELSE 'NO' END AS "Lead Active?",
         MAX(ji.updated) AS "Last Update",
         COUNT(DISTINCT ji.id) AS "Issue Count",
         MAX(cg.author) AS "Last Update User Key"
  FROM jiraissue ji
  JOIN project p ON p.id = ji.project
  JOIN app_user au ON p.lead = au.user_key
  JOIN cwd_user cu ON au.lower_user_name = cu.lower_user_name
  LEFT JOIN propertyentry pe ON p.id = pe.entity_id AND pe.property_key = 'jira.archiving.projects'
  LEFT JOIN changegroup cg ON ji.id = cg.issueid
  GROUP BY p.pkey, p.id, p.originalkey, pe.entity_id, cu.lower_user_name, cu.lower_email_address, cu.active
) ss
LEFT JOIN app_user au2 ON ss."Last Update User Key" = au2.user_key
LEFT JOIN cwd_user cu2 ON au2.lower_user_name = cu2.lower_user_name;
