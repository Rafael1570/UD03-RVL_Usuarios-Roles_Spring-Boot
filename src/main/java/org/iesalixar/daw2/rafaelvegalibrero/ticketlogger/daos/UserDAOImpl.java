package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.Role;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAOImpl implements UserDAO {
    private static final String SELECT = """
        SELECT u.*, r.id AS role_id, r.name AS role_name
        FROM users u LEFT JOIN user_roles ur ON ur.user_id=u.id
        LEFT JOIN roles r ON r.id=ur.role_id
        """;
    private final JdbcTemplate jdbc;

    public UserDAOImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<User> findAll() { return selectUsers(SELECT + " ORDER BY u.username, r.name"); }
    @Override
    public Optional<User> findById(Long id) {
        return selectUsers(SELECT + " WHERE u.id=? ORDER BY r.name", id).stream().findFirst();
    }
    private List<User> selectUsers(String sql, Object... args) {
        return jdbc.query(sql, (ResultSetExtractor<List<User>>) rs -> {
            var users = new LinkedHashMap<Long, User>();
            while (rs.next()) {
                Long id = rs.getLong("id");
                User user = users.get(id);
                if (user == null) {
                    user = mapUser(rs);
                    users.put(id, user);
                }
                long roleId = rs.getLong("role_id");
                if (!rs.wasNull()) user.getRoles().add(new Role(roleId, rs.getString("role_name")));
            }
            return new ArrayList<>(users.values());
        }, args);
    }
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("passwordHash"));
        user.setActive(rs.getBoolean("active"));
        user.setAccountNonLocked(rs.getBoolean("accountNonLocked"));
        user.setLastPasswordChange(rs.getTimestamp("lastPasswordChange").toLocalDateTime());
        user.setPasswordExpiresAt(rs.getTimestamp("passwordExpiresAt").toLocalDateTime());
        user.setFailedLoginAttempts(rs.getInt("failedLoginAttempts"));
        user.setEmailVerified(rs.getBoolean("emailVerified"));
        user.setMustChangePassword(rs.getBoolean("mustChangePassword"));
        return user;
    }
    @Override
    public boolean usernameExists(String username, Long excludedId) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username)=LOWER(?)";
        Integer count = excludedId == null
                ? jdbc.queryForObject(sql, Integer.class, username)
                : jdbc.queryForObject(sql + " AND id<>?", Integer.class, username, excludedId);
        return count != null && count > 0;
    }
    @Override
    public Long insert(User user) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("""
                INSERT INTO users (username, passwordHash, active, accountNonLocked, lastPasswordChange,
                    passwordExpiresAt, failedLoginAttempts, emailVerified, mustChangePassword)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            bind(statement, user);
            return statement;
        }, keys);
        user.setId(keys.getKey().longValue());
        return user.getId();
    }
    private void bind(PreparedStatement statement, User user) throws SQLException {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPasswordHash());
        statement.setBoolean(3, user.getActive());
        statement.setBoolean(4, user.getAccountNonLocked());
        statement.setTimestamp(5, Timestamp.valueOf(user.getLastPasswordChange()));
        statement.setTimestamp(6, Timestamp.valueOf(user.getPasswordExpiresAt()));
        statement.setInt(7, user.getFailedLoginAttempts());
        statement.setBoolean(8, user.getEmailVerified());
        statement.setBoolean(9, user.getMustChangePassword());
    }
    @Override
    public int update(User user) {
        return jdbc.update(connection -> {
            var statement = connection.prepareStatement("""
                UPDATE users SET username=?, passwordHash=?, active=?, accountNonLocked=?, lastPasswordChange=?,
                    passwordExpiresAt=?, failedLoginAttempts=?, emailVerified=?, mustChangePassword=? WHERE id=?
                """);
            bind(statement, user);
            statement.setLong(10, user.getId());
            return statement;
        });
    }
    @Override
    public int delete(Long id) { return jdbc.update("DELETE FROM users WHERE id=?", id); }

    // El servicio llama a este método en la misma transacción que el alta o edición del usuario.
    @Override
    public void replaceRoles(Long userId, Collection<Long> roleIds) {
        jdbc.update("DELETE FROM user_roles WHERE user_id=?", userId);
        for (Long roleId : roleIds) {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
        }
    }
}
