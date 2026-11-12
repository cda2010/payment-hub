CREATE TABLE coffre (
    id              UUID PRIMARY KEY,
    owner_username  VARCHAR(120) NOT NULL,
    name            VARCHAR(120) NOT NULL,
    balance         NUMERIC(18, 2) NOT NULL,
    target_amount   NUMERIC(18, 2) NOT NULL,
    goal            VARCHAR(20) NOT NULL,
    locked          BOOLEAN NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX coffre_owner_idx ON coffre (owner_username);

CREATE TABLE tontine (
    id            UUID PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    contribution  NUMERIC(18, 2) NOT NULL,
    frequency     VARCHAR(20) NOT NULL,
    round_no      INT NOT NULL,
    invite_code   VARCHAR(16) NOT NULL UNIQUE,
    status        VARCHAR(20) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL
);

CREATE TABLE tontine_member (
    id             UUID PRIMARY KEY,
    tontine_id     UUID NOT NULL REFERENCES tontine (id),
    username       VARCHAR(120) NOT NULL,
    display_name   VARCHAR(120) NOT NULL,
    turn_no        INT NOT NULL,
    paid           BOOLEAN NOT NULL,
    admin_member   BOOLEAN NOT NULL,
    UNIQUE (tontine_id, username),
    UNIQUE (tontine_id, turn_no)
);

CREATE INDEX tontine_member_user_idx ON tontine_member (username);
