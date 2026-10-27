-- Datos de ejemplo. Las asignaciones existentes se conservan en los reinicios.
INSERT INTO roles (name) SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE LOWER(name)=LOWER('ADMIN'));
INSERT INTO roles (name) SELECT 'USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE LOWER(name)=LOWER('USER'));
INSERT INTO roles (name) SELECT 'EDITOR' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE LOWER(name)=LOWER('EDITOR'));
INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange, passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword)
SELECT 'admin', '$2b$12$dDrjtPxodpLnz37cfiFggeNHEKoSBsx7YS/gqkLieNUEbChr0mDP6', TRUE, TRUE, '2026-10-27 12:00:00', '2027-01-27 12:00:00', 0, TRUE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(username) = 'admin');
INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange, passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword)
SELECT 'jdoe', '$2b$12$dDrjtPxodpLnz37cfiFggeNHEKoSBsx7YS/gqkLieNUEbChr0mDP6', TRUE, TRUE, '2026-10-27 12:00:00', '2027-01-27 12:00:00', 1, FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(username) = 'jdoe');
INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange, passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword)
SELECT 'maria', '$2b$12$dDrjtPxodpLnz37cfiFggeNHEKoSBsx7YS/gqkLieNUEbChr0mDP6', TRUE, TRUE, '2026-10-27 12:00:00', '2027-01-27 12:00:00', 0, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(username) = 'maria');
INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange, passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword)
SELECT 'blockeduser', '$2b$12$dDrjtPxodpLnz37cfiFggeNHEKoSBsx7YS/gqkLieNUEbChr0mDP6', FALSE, FALSE, '2026-10-27 12:00:00', '2027-01-27 12:00:00', 5, FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(username) = 'blockeduser');
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE u.username IN ('admin', 'jdoe', 'maria', 'blockeduser')
  AND (r.name='USER' OR (u.username='admin' AND r.name='ADMIN'))
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id);
