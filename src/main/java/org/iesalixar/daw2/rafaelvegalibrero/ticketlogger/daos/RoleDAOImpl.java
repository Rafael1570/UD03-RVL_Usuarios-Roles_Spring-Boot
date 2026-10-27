package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDAOImpl implements RoleDAO {
    private final JdbcTemplate jdbc;
    private final RowMapper<Role> mapper = (rs, row) -> new Role(rs.getLong("id"), rs.getString("name"));

    public RoleDAOImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    public List<Role> findAll() {
        return jdbc.query("SELECT id, name FROM roles ORDER BY name", mapper);
    }
    @Override
    public Optional<Role> findById(Long id) {
        return jdbc.query("SELECT id, name FROM roles WHERE id=?", mapper, id).stream().findFirst();
    }
    @Override
    public List<Role> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        return jdbc.query("SELECT id, name FROM roles WHERE id IN (" + placeholders + ") ORDER BY name", mapper, ids.toArray());
    }
    @Override
    public boolean nameExists(String name, Long excludedId) {
        String sql = "SELECT COUNT(*) FROM roles WHERE LOWER(name)=LOWER(?)";
        Integer count = excludedId == null
                ? jdbc.queryForObject(sql, Integer.class, name)
                : jdbc.queryForObject(sql + " AND id<>?", Integer.class, name, excludedId);
        return count != null && count > 0;
    }
    @Override
    public Long insert(Role role) {
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO roles(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, role.getName());
            return statement;
        }, keys);
        role.setId(keys.getKey().longValue());
        return role.getId();
    }
    @Override
    public int update(Role role) {
        return jdbc.update("UPDATE roles SET name=? WHERE id=?", role.getName(), role.getId());
    }
    @Override
    public int delete(Long id) { return jdbc.update("DELETE FROM roles WHERE id=?", id); }
    @Override
    public boolean isAssigned(Long id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM user_roles WHERE role_id=?", Integer.class, id);
        return count != null && count > 0;
    }
}
