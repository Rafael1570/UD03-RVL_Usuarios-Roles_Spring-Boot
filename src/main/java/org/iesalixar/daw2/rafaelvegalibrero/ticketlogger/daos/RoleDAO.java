package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.Role;

public interface RoleDAO {
    List<Role> findAll();
    Optional<Role> findById(Long id);
    List<Role> findByIds(Collection<Long> ids);
    boolean nameExists(String name, Long excludedId);
    Long insert(Role role);
    int update(Role role);
    int delete(Long id);
    boolean isAssigned(Long id);
}
