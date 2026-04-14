--liquibase formatted sql

--changeset taskflow:V4-seed-data context:seed
INSERT INTO users (id, name, email, password_hash, created_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Test User',
    'test@example.com',
    crypt('password123', gen_salt('bf', 12)),
    NOW()
)
ON CONFLICT (email) DO NOTHING;

INSERT INTO projects (id, name, description, owner_id, created_at)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Greening India Launch',
    'Initial seeded project for reviewer verification.',
    '11111111-1111-1111-1111-111111111111',
    NOW()
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO tasks (id, title, description, status, priority, project_id, assignee_id, created_by, due_date, created_at, updated_at)
VALUES
(
    '33333333-3333-3333-3333-333333333331',
    'Draft API contract',
    'Prepare the first version of the TaskFlow backend API contract.',
    'todo',
    'high',
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    CURRENT_DATE + INTERVAL '3 days',
    NOW(),
    NOW()
),
(
    '33333333-3333-3333-3333-333333333332',
    'Implement authentication',
    'Build register and login flows with JWT protection.',
    'in_progress',
    'high',
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    CURRENT_DATE + INTERVAL '5 days',
    NOW(),
    NOW()
),
(
    '33333333-3333-3333-3333-333333333333',
    'Set up local Docker stack',
    'Postgres and backend containers should boot with one command.',
    'done',
    'medium',
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    '11111111-1111-1111-1111-111111111111',
    CURRENT_DATE + INTERVAL '1 day',
    NOW(),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

--rollback DELETE FROM tasks WHERE id IN (
--rollback   '33333333-3333-3333-3333-333333333331',
--rollback   '33333333-3333-3333-3333-333333333332',
--rollback   '33333333-3333-3333-3333-333333333333'
--rollback );
--rollback DELETE FROM projects WHERE id = '22222222-2222-2222-2222-222222222222';
--rollback DELETE FROM users WHERE id = '11111111-1111-1111-1111-111111111111';
