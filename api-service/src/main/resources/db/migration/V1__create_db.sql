-- ===================================================================
-- 1. Primary Table: job
-- ===================================================================
CREATE TABLE job (
                     id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
                     job_type      VARCHAR(100)  NOT NULL,
                     payload       JSONB         NOT NULL,
                     status        VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING','QUEUED','PROCESSING',
                                           'RETRYING','COMPLETED','FAILED','DLQ')),
                     retry_count   INTEGER       NOT NULL DEFAULT 0,
                     error_message TEXT,
                     client_req_id VARCHAR(128)  UNIQUE,
                     tenant_id     VARCHAR(64),
                     created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                     updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- ===================================================================
-- 2. Trigger to Auto-Update updated_at Timestamp
-- ===================================================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_job_updated_at
    BEFORE UPDATE ON job
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

--Indexes for performance optimization

-- Filter by job status for worker polling
CREATE INDEX idx_job_status         ON job (status);

-- Polling for jobs created in a time range
CREATE INDEX idx_job_created_at     ON job (created_at DESC);

-- Filter by job type for analytics
CREATE INDEX idx_job_type           ON job (job_type);

-- Composite for worker queries: PENDING jobs ordered by creation
CREATE INDEX idx_job_status_created ON job (status, created_at);

-- Tenant isolation queries
CREATE INDEX idx_job_tenant         ON job (tenant_id);

-- GIN index for JSONB payload deep-querying
CREATE INDEX idx_job_payload_gin    ON job USING GIN (payload);