CREATE TABLE devices
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    brand         VARCHAR(255) NOT NULL,
    state         VARCHAR(50)  NOT NULL CHECK (state IN ('AVAILABLE', 'IN_USE', 'INACTIVE')),
    creation_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
