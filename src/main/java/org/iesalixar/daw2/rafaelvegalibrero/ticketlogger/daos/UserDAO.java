package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.User;

public interface UserDAO {
    List<User> findAll();
    Optional<User> findById(Long id);
    boolean usernameExists(String username, Long excludedId);
    Long insert(User user);
    int update(User user);
    int delete(Long id);
    void replaceRoles(Long userId, Collection<Long> roleIds);
}
