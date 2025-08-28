-- PostgreSQL
SELECT
    ss.*,
    CASE 
        WHEN ss."Last Update" < CURRENT_DATE - INTERVAL '12 months' THEN 'NO'
        ELSE 'YES'
    END AS "Updated in the last 12 months?"
FROM (
    SELECT DISTINCT
        p.pkey AS "Project Key",
        CASE 
            WHEN p.pkey = p.originalkey THEN NULL
            ELSE p.originalkey
        END AS "Original Key",
        CASE 
            WHEN p.id = pe.entity_id THEN 'YES'
            ELSE 'NO'
        END AS "Project Archived?",
        cu.lower_user_name AS "Project Lead",
        cu.lower_email_address AS "Lead Email",
        CASE 
            WHEN cu.active = 1 THEN 'YES'
            ELSE 'NO'
        END AS "Lead Active?",
        MAX(ji.updated) AS "Last Update",
        COUNT(DISTINCT ji.id) AS "Issue Count"
    FROM jiraissue ji
    JOIN project p 
        ON p.id = ji.project
    JOIN app_user au 
        ON p.lead = au.user_key
    JOIN cwd_user cu 
        ON au.lower_user_name = cu.lower_user_name
    LEFT JOIN propertyentry pe 
        ON p.id = pe.entity_id
       AND pe.property_key = 'jira.archiving.projects'
    GROUP BY 1, 2, 3, 4, 5, 6
) ss;
