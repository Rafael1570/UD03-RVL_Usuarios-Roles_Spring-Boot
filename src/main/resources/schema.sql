CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
    passwordHash VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    accountNonLocked BOOLEAN NOT NULL DEFAULT TRUE,
    lastPasswordChange DATETIME NOT NULL,
    passwordExpiresAt DATETIME NOT NULL,
    failedLoginAttempts INT NOT NULL DEFAULT 0,
    emailVerified BOOLEAN NOT NULL DEFAULT FALSE,
    mustChangePassword BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_failed_attempts CHECK (failedLoginAttempts >= 0)
);
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);
