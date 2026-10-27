package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos.RoleDAO;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos.UserDAO;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.User;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.forms.UserForm;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly=true)
public class UserService {
    private final UserDAO users;
    private final RoleDAO roles;
    private final PasswordEncoder encoder;

    public UserService(UserDAO users, RoleDAO roles, PasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
    }
    public List<User> findAll() { return users.findAll(); }
    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    public UserForm toForm(User user) {
        var form = new UserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setActive(user.getActive());
        form.setAccountNonLocked(user.getAccountNonLocked());
        form.setLastPasswordChange(user.getLastPasswordChange());
        form.setFailedLoginAttempts(user.getFailedLoginAttempts());
        form.setEmailVerified(user.getEmailVerified());
        form.setMustChangePassword(user.getMustChangePassword());
        form.setRoleIds(user.getRoles().stream().map(role -> role.getId()).toList());
        return form;
    }
    private LinkedHashSet<Long> validateRoles(UserForm form) {
        if (form.getRoleIds() == null || form.getRoleIds().isEmpty())
            throw new FormException("roleIds", "validation.user.roles.required");
        var ids = new LinkedHashSet<>(form.getRoleIds());
        if (ids.stream().anyMatch(id -> id == null || id <= 0) || roles.findByIds(ids).size() != ids.size())
            throw new FormException("roleIds", "validation.user.roles.invalid");
        return ids;
    }
    private void validatePassword(String password, boolean creating) {
        if (!creating && (password == null || password.isEmpty())) return;
        if (password == null || password.isBlank()) throw new FormException("password", "validation.user.password.required");
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new FormException("password", "validation.user.password.size");
    }
    private void applyForm(User user, UserForm form) {
        user.setUsername(form.getUsername().trim());
        if (users.usernameExists(user.getUsername(), user.getId()))
            throw new FormException("username", "validation.user.duplicate");
        if (form.getPassword() != null && !form.getPassword().isEmpty())
            user.setPasswordHash(encoder.encode(form.getPassword()));
        user.setActive(form.isActive());
        user.setAccountNonLocked(form.isAccountNonLocked());
        user.setLastPasswordChange(form.getLastPasswordChange());
        user.setPasswordExpiresAt(form.getLastPasswordChange().plusMonths(3));
        user.setFailedLoginAttempts(form.getFailedLoginAttempts());
        user.setEmailVerified(form.isEmailVerified());
        user.setMustChangePassword(form.isMustChangePassword());
    }
    @Transactional
    public Long create(UserForm form) {
        var ids = validateRoles(form);
        validatePassword(form.getPassword(), true);
        User user = new User();
        applyForm(user, form);
        Long id = users.insert(user);
        users.replaceRoles(id, ids);
        return id;
    }
    @Transactional
    public void update(UserForm form) {
        User user = findById(form.getId());
        var ids = validateRoles(form);
        validatePassword(form.getPassword(), false);
        applyForm(user, form);
        if (users.update(user) != 1) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        users.replaceRoles(user.getId(), ids);
    }
    @Transactional
    public void delete(Long id) {
        findById(id);
        users.delete(id);
    }
}
