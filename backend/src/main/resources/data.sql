INSERT INTO teams (name, parent_team_id)
SELECT '로데이노베이션', NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM teams
    WHERE name = '로데이노베이션'
      AND parent_team_id IS NULL
);

INSERT INTO teams (name, parent_team_id)
SELECT '경영전략본부', parent.id
FROM teams parent
WHERE parent.name = '로데이노베이션'
  AND parent.parent_team_id IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM teams child
      WHERE child.name = '경영전략본부'
        AND child.parent_team_id = parent.id
  );

INSERT INTO teams (name, parent_team_id)
SELECT '경영지원부문', parent.id
FROM teams parent
WHERE parent.name = '경영전략본부'
  AND NOT EXISTS (
      SELECT 1
      FROM teams child
      WHERE child.name = '경영지원부문'
        AND child.parent_team_id = parent.id
  );

INSERT INTO teams (name, parent_team_id)
SELECT child_name, parent.id
FROM teams parent
CROSS JOIN (
    VALUES ('인사교육팀'), ('구매팀'), ('재무지원팀')
) AS children(child_name)
WHERE parent.name = '경영지원부문'
  AND NOT EXISTS (
      SELECT 1
      FROM teams child
      WHERE child.name = children.child_name
        AND child.parent_team_id = parent.id
  );

INSERT INTO work_policies (name, latitude, longitude, checkin_radius_m, checkout_radius_m, checkout_grace_minutes, team_id)
SELECT
    '기본 정책 ' || t.id,
    ROUND((37.300000 + random() * 0.500000)::numeric, 6)::double precision,
    ROUND((126.700000 + random() * 0.700000)::numeric, 6)::double precision,
    (100 + FLOOR(random() * 151))::int,
    (200 + FLOOR(random() * 201))::int,
    (5 + FLOOR(random() * 16))::int,
    t.id
FROM teams t
WHERE NOT EXISTS (
    SELECT 1
    FROM work_policies wp
    WHERE wp.team_id = t.id
);

-- 관리자 유저 (비밀번호: Test1234!)
INSERT INTO users (login_id, password_hash, email, name, role_level, team_id, policy_id, active, hr_authority, created_at, updated_at)
SELECT
    'admin',
    '$2a$10$AxTw/7.TZ1oWYOReGjx6qONpqeCKqj7xVc4b1EhtBv3znaiyAtmF2',
    'admin@rodeinnovation.com',
    '관리자',
    'DEPARTMENT_HEAD',
    t.id,
    wp.id,
    true,
    true,
    NOW(),
    NOW()
FROM teams t
JOIN work_policies wp ON wp.team_id = t.id
WHERE t.name = '경영전략본부'
  AND NOT EXISTS (
      SELECT 1 FROM users WHERE login_id = 'admin'
  );

