package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services;

import java.util.List;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos.RoleDAO;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.Role;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class RoleService {
    private final RoleDAO roles;
    public RoleService(RoleDAO roles) { this.roles = roles; }

    public List<Role> findAll() { return roles.findAll(); }
    public Role findById(Long id) {
        return roles.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private void validateName(Role role, Long excludedId) {
        role.setName(role.getName().trim());
        if (roles.nameExists(role.getName(), excludedId)) throw new FormException("name", "validation.role.duplicate");
    }
    @Transactional
    public Long create(Role role) {
        role.setId(null);
        validateName(role, null);
        return roles.insert(role);
    }
    @Transactional
    public void update(Role role) {
        findById(role.getId());
        validateName(role, role.getId());
        if (roles.update(role) != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    @Transactional
    public void delete(Long id) {
        findById(id);
        if (roles.isAssigned(id)) throw new FormException(null, "role.inUse");
        roles.delete(id);
    }
}
