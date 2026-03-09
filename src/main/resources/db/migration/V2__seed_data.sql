INSERT INTO clients (name, email, phone, company) VALUES
    ('John Doe',   'john@example.com', '+1234567890', 'Acme Corp'),
    ('Jane Smith',  'jane@example.com', '+0987654321', 'Tech Solutions'),
    ('Bob Wilson',  'bob@example.com',  '+1122334455', 'Global Services');

INSERT INTO providers (name, email, phone, service_type) VALUES
    ('Cloud Provider',      'cloud@example.com', '+1111111111', 'CLOUD'),
    ('Security Solutions',  'sec@example.com',   '+2222222222', 'SECURITY'),
    ('Data Analytics Co',   'data@example.com',  '+3333333333', 'ANALYTICS');

INSERT INTO tasks (title, description, status, client_id, provider_id) VALUES
    ('Setup cloud infrastructure', 'Configure AWS services for production',  'NEW',         1, 1),
    ('Security audit',             'Perform full security review',           'IN_PROGRESS', 1, 2),
    ('Data migration',             'Migrate legacy database to new schema',  'NEW',         2, 3),
    ('API integration',            'Integrate payment gateway',              'DONE',        2, 1),
    ('Deploy monitoring',          'Setup monitoring and alerting tools',     'NEW',         3, NULL);
