INSERT INTO public.subscription_providers (name, price)
VALUES
    ('YouTube Premium', 11.99),
    ('Netflix', 15.49),
    ('Spotify', 9.99),
    ('Apple Music', 10.99),
    ('Disney+', 8.99)
ON CONFLICT (name) DO NOTHING;

INSERT INTO public.users (username, email, full_name)
SELECT
    'user_' || i,
    'user_' || i || '@example.com',
    CASE
        WHEN i % 3 = 0 THEN 'User ' || i || ' Smith'
        WHEN i % 3 = 1 THEN 'User ' || i || ' Johnson'
        ELSE 'User ' || i || ' Williams'
        END
FROM generate_series(1, 10) AS i
ON CONFLICT (username) DO NOTHING;

INSERT INTO public.subscriptions (user_id, service_id, start_date, end_date, active)
SELECT
    u.id,
    sp.id,
    CURRENT_TIMESTAMP - (random() * 365 || ' days')::interval,
    CASE
        WHEN random() > 0.3 THEN CURRENT_TIMESTAMP + (random() * 365 || ' days')::interval
        ELSE NULL
        END,
    random() > 0.2
FROM
    (SELECT id FROM public.users ORDER BY random() LIMIT 10) AS u
        CROSS JOIN
    (SELECT id FROM public.subscription_providers ORDER BY random() LIMIT 5) AS sp
ORDER BY random()
LIMIT 30
ON CONFLICT (user_id, service_id) DO NOTHING;