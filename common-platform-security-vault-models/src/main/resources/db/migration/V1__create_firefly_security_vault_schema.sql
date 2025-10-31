-- =====================================================
-- Firefly Core Banking - Security Vault Schema
-- =====================================================
-- This schema manages secure credential storage for providers,
-- integrations, and external services for the Firefly 
-- open-source core banking platform.
-- =====================================================

-- =====================================================
-- CREDENTIAL TYPE TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_credential_types_code ON credential_types(code);
CREATE INDEX idx_credential_types_category ON credential_types(category);
CREATE INDEX idx_credential_types_active ON credential_types(active);

-- Insert default credential types
INSERT INTO credential_types (code, name, description, category, active) VALUES
    ('API_KEY', 'API Key', 'Simple API key authentication', 'AUTHENTICATION', true),
    ('API_SECRET', 'API Secret', 'API secret for HMAC or similar', 'AUTHENTICATION', true),
    ('OAUTH2_CLIENT', 'OAuth 2.0 Client', 'OAuth 2.0 client credentials', 'AUTHENTICATION', true),
    ('JWT_TOKEN', 'JWT Token', 'JSON Web Token', 'AUTHENTICATION', true),
    ('BASIC_AUTH', 'Basic Authentication', 'Username and password', 'AUTHENTICATION', true),
    ('BEARER_TOKEN', 'Bearer Token', 'Bearer authentication token', 'AUTHENTICATION', true),
    ('SSH_KEY', 'SSH Key', 'SSH private/public key pair', 'AUTHENTICATION', true),
    ('DATABASE_CREDENTIALS', 'Database Credentials', 'Database username and password', 'DATABASE', true),
    ('ENCRYPTION_KEY', 'Encryption Key', 'Symmetric encryption key', 'ENCRYPTION', true),
    ('CERTIFICATE', 'Certificate', 'SSL/TLS certificate', 'ENCRYPTION', true),
    ('WEBHOOK_SECRET', 'Webhook Secret', 'Webhook signing secret', 'INTEGRATION', true),
    ('SERVICE_ACCOUNT', 'Service Account', 'Service account credentials', 'AUTHENTICATION', true);

-- =====================================================
-- CREDENTIAL STATUS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_statuses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_credential_statuses_code ON credential_statuses(code);
CREATE INDEX idx_credential_statuses_active ON credential_statuses(active);

-- Insert default credential statuses
INSERT INTO credential_statuses (code, name, description, active) VALUES
    ('ACTIVE', 'Active', 'Credential is active and operational', true),
    ('INACTIVE', 'Inactive', 'Credential is inactive', true),
    ('EXPIRED', 'Expired', 'Credential has expired', true),
    ('REVOKED', 'Revoked', 'Credential has been revoked', true),
    ('ROTATING', 'Rotating', 'Credential is being rotated', true),
    ('COMPROMISED', 'Compromised', 'Credential has been compromised', true);

-- =====================================================
-- ENVIRONMENT TYPE TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS environment_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_environment_types_code ON environment_types(code);
CREATE INDEX idx_environment_types_active ON environment_types(active);

-- Insert default environment types
INSERT INTO environment_types (code, name, description, active) VALUES
    ('DEVELOPMENT', 'Development', 'Development environment', true),
    ('TESTING', 'Testing', 'Testing/QA environment', true),
    ('STAGING', 'Staging', 'Staging/Pre-production environment', true),
    ('PRODUCTION', 'Production', 'Production environment', true),
    ('SANDBOX', 'Sandbox', 'Sandbox/Demo environment', true);

-- =====================================================
-- CREDENTIALS TABLE (Main Vault Storage)
-- =====================================================
CREATE TABLE IF NOT EXISTS credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    credential_type_id UUID NOT NULL,
    credential_status_id UUID NOT NULL,
    environment_type_id UUID NOT NULL,
    
    -- Reference to external entity using these credentials
    provider_id UUID,
    integration_id UUID,
    service_id UUID,
    tenant_id UUID,
    
    -- Encrypted credential data (stored as encrypted JSON)
    encrypted_value TEXT NOT NULL,
    encryption_algorithm VARCHAR(50) NOT NULL DEFAULT 'AES-256-GCM',
    encryption_key_id VARCHAR(100) NOT NULL,
    encryption_iv TEXT,
    
    -- Credential metadata
    credential_owner VARCHAR(255),
    credential_contact_email VARCHAR(255),
    
    -- Rotation and expiration
    expires_at TIMESTAMP,
    rotate_before_days INTEGER DEFAULT 30,
    last_rotated_at TIMESTAMP,
    rotation_enabled BOOLEAN DEFAULT FALSE,
    auto_rotation_days INTEGER,
    
    -- Usage tracking
    last_used_at TIMESTAMP,
    usage_count BIGINT DEFAULT 0,
    last_accessed_by VARCHAR(255),
    
    -- Access control
    access_scope VARCHAR(50) DEFAULT 'INTERNAL',
    allowed_services TEXT,
    allowed_ips TEXT,
    allowed_environments TEXT,
    
    -- Security flags
    is_sensitive BOOLEAN DEFAULT TRUE,
    require_approval_for_access BOOLEAN DEFAULT FALSE,
    audit_all_access BOOLEAN DEFAULT TRUE,
    mask_in_logs BOOLEAN DEFAULT TRUE,
    
    -- Backup and recovery
    backup_enabled BOOLEAN DEFAULT TRUE,
    backup_location VARCHAR(500),
    
    -- Tags for organization
    tags TEXT,
    metadata TEXT,
    
    -- System fields
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    
    CONSTRAINT fk_credential_type FOREIGN KEY (credential_type_id) REFERENCES credential_types(id),
    CONSTRAINT fk_credential_status FOREIGN KEY (credential_status_id) REFERENCES credential_statuses(id),
    CONSTRAINT fk_environment_type FOREIGN KEY (environment_type_id) REFERENCES environment_types(id)
);

CREATE INDEX idx_credentials_code ON credentials(code);
CREATE INDEX idx_credentials_type ON credentials(credential_type_id);
CREATE INDEX idx_credentials_status ON credentials(credential_status_id);
CREATE INDEX idx_credentials_environment ON credentials(environment_type_id);
CREATE INDEX idx_credentials_provider ON credentials(provider_id);
CREATE INDEX idx_credentials_tenant ON credentials(tenant_id);
CREATE INDEX idx_credentials_active ON credentials(active);
CREATE INDEX idx_credentials_expires_at ON credentials(expires_at);
CREATE INDEX idx_credentials_encryption_key_id ON credentials(encryption_key_id);

-- Unique constraint for code within environment
CREATE UNIQUE INDEX idx_credentials_code_environment ON credentials(code, environment_type_id) WHERE active = TRUE;

-- =====================================================
-- CREDENTIAL VERSIONS TABLE (Version History)
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credential_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    encrypted_value TEXT NOT NULL,
    encryption_algorithm VARCHAR(50) NOT NULL,
    encryption_key_id VARCHAR(100) NOT NULL,
    encryption_iv TEXT,
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    is_current BOOLEAN DEFAULT FALSE,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rotation_reason VARCHAR(255),
    metadata TEXT,
    
    CONSTRAINT fk_credential_version FOREIGN KEY (credential_id) REFERENCES credentials(id) ON DELETE CASCADE
);

CREATE INDEX idx_credential_versions_credential ON credential_versions(credential_id);
CREATE INDEX idx_credential_versions_current ON credential_versions(is_current);
CREATE INDEX idx_credential_versions_valid_from ON credential_versions(valid_from);
CREATE INDEX idx_credential_versions_valid_until ON credential_versions(valid_until);

-- Unique constraint for current version
CREATE UNIQUE INDEX idx_credential_versions_current_unique ON credential_versions(credential_id) WHERE is_current = TRUE;

-- =====================================================
-- CREDENTIAL ACCESS LOG TABLE (Audit Trail)
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_access_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credential_id UUID NOT NULL,
    access_type VARCHAR(50) NOT NULL,
    accessed_by VARCHAR(255),
    accessed_by_user_id UUID,
    accessed_by_service VARCHAR(255),
    access_ip VARCHAR(50),
    access_location VARCHAR(255),
    access_result VARCHAR(50) NOT NULL,
    access_reason TEXT,
    credential_version_id UUID,
    decryption_successful BOOLEAN,
    error_message TEXT,
    access_duration_ms INTEGER,
    metadata TEXT,
    accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_credential_access_log FOREIGN KEY (credential_id) REFERENCES credentials(id) ON DELETE CASCADE
);

CREATE INDEX idx_credential_access_logs_credential ON credential_access_logs(credential_id);
CREATE INDEX idx_credential_access_logs_accessed_by ON credential_access_logs(accessed_by);
CREATE INDEX idx_credential_access_logs_accessed_at ON credential_access_logs(accessed_at);
CREATE INDEX idx_credential_access_logs_access_result ON credential_access_logs(access_result);
CREATE INDEX idx_credential_access_logs_access_type ON credential_access_logs(access_type);

-- =====================================================
-- ENCRYPTION KEY METADATA TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS encryption_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_id VARCHAR(100) NOT NULL UNIQUE,
    key_name VARCHAR(255) NOT NULL,
    key_type VARCHAR(50) NOT NULL,
    key_algorithm VARCHAR(50) NOT NULL,
    key_provider VARCHAR(100),
    key_location VARCHAR(500),
    key_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_master_key BOOLEAN DEFAULT FALSE,
    key_purpose VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    rotated_at TIMESTAMP,
    rotation_schedule_days INTEGER,
    metadata TEXT,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_by UUID,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_encryption_keys_key_id ON encryption_keys(key_id);
CREATE INDEX idx_encryption_keys_key_status ON encryption_keys(key_status);
CREATE INDEX idx_encryption_keys_active ON encryption_keys(active);
CREATE INDEX idx_encryption_keys_is_master_key ON encryption_keys(is_master_key);

-- =====================================================
-- CREDENTIAL SHARING TABLE (Credential Access Grants)
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_shares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credential_id UUID NOT NULL,
    shared_with_tenant_id UUID,
    shared_with_service VARCHAR(255),
    shared_with_user_id UUID,
    share_type VARCHAR(50) NOT NULL,
    access_level VARCHAR(50) NOT NULL DEFAULT 'READ_ONLY',
    valid_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_until TIMESTAMP,
    max_access_count INTEGER,
    current_access_count INTEGER DEFAULT 0,
    allowed_operations TEXT,
    shared_by UUID,
    approval_required BOOLEAN DEFAULT FALSE,
    approval_status VARCHAR(50),
    approved_by UUID,
    approved_at TIMESTAMP,
    share_reason TEXT,
    metadata TEXT,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_credential_share FOREIGN KEY (credential_id) REFERENCES credentials(id) ON DELETE CASCADE
);

CREATE INDEX idx_credential_shares_credential ON credential_shares(credential_id);
CREATE INDEX idx_credential_shares_tenant ON credential_shares(shared_with_tenant_id);
CREATE INDEX idx_credential_shares_user ON credential_shares(shared_with_user_id);
CREATE INDEX idx_credential_shares_service ON credential_shares(shared_with_service);
CREATE INDEX idx_credential_shares_active ON credential_shares(active);
CREATE INDEX idx_credential_shares_valid_from ON credential_shares(valid_from);
CREATE INDEX idx_credential_shares_valid_until ON credential_shares(valid_until);

-- =====================================================
-- CREDENTIAL ROTATION POLICY TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_rotation_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    credential_type_id UUID,
    environment_type_id UUID,
    rotation_interval_days INTEGER NOT NULL,
    rotation_warning_days INTEGER DEFAULT 30,
    auto_rotation_enabled BOOLEAN DEFAULT FALSE,
    require_manual_approval BOOLEAN DEFAULT TRUE,
    notify_before_days INTEGER DEFAULT 7,
    notification_recipients TEXT,
    policy_enforcement VARCHAR(50) DEFAULT 'ADVISORY',
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    
    CONSTRAINT fk_rotation_policy_credential_type FOREIGN KEY (credential_type_id) REFERENCES credential_types(id),
    CONSTRAINT fk_rotation_policy_environment_type FOREIGN KEY (environment_type_id) REFERENCES environment_types(id)
);

CREATE INDEX idx_rotation_policies_credential_type ON credential_rotation_policies(credential_type_id);
CREATE INDEX idx_rotation_policies_environment_type ON credential_rotation_policies(environment_type_id);
CREATE INDEX idx_rotation_policies_active ON credential_rotation_policies(active);

-- =====================================================
-- CREDENTIAL ALERTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS credential_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    credential_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    alert_message TEXT NOT NULL,
    alert_details TEXT,
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_by UUID,
    acknowledged_at TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_by UUID,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_sent_at TIMESTAMP,
    metadata TEXT,
    
    CONSTRAINT fk_credential_alert FOREIGN KEY (credential_id) REFERENCES credentials(id) ON DELETE CASCADE
);

CREATE INDEX idx_credential_alerts_credential ON credential_alerts(credential_id);
CREATE INDEX idx_credential_alerts_type ON credential_alerts(alert_type);
CREATE INDEX idx_credential_alerts_severity ON credential_alerts(severity);
CREATE INDEX idx_credential_alerts_triggered_at ON credential_alerts(triggered_at);
CREATE INDEX idx_credential_alerts_acknowledged ON credential_alerts(acknowledged);
CREATE INDEX idx_credential_alerts_resolved ON credential_alerts(resolved);
