package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos.RoleDAO;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.daos.UserDAO;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.User;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.forms.UserForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsersRolesIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserDAO users;
    @Autowired RoleDAO roles;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    private Long roleId(String name) {
        return roles.findAll().stream().filter(role -> role.getName().equals(name)).findFirst().orElseThrow().getId();
    }
    private User user(String name) {
        return users.findAll().stream().filter(user -> user.getUsername().equals(name)).findFirst().orElseThrow();
    }
    private MockHttpServletRequestBuilder userRequest(String path, String username, Long... ids) {
        var request = post(path).param("username", username).param("password", "NuevaClave123!")
                .param("lastPasswordChange", "2026-10-27T10:00")
                .param("failedLoginAttempts", "0").param("active", "true")
                .param("accountNonLocked", "true").param("emailVerified", "true");
        if (ids.length > 0) request.param("roleIds", Arrays.stream(ids).map(String::valueOf).toArray(String[]::new));
        return request;
    }
    private MockHttpServletRequestBuilder replace(MockHttpServletRequestBuilder builder, String key, String value) {
        return builder.with(request -> {
            request.setParameter(key, value);
            return request;
        });
    }

    @Test
    void rendersAllScreensAndNeverExposesPasswordHashes() throws Exception {
        Long userId = user("admin").getId();
        Long roleId = roleId("ADMIN");
        for (String path : List.of("/", "/users", "/users/new", "/users/edit?id=" + userId,
                "/users/detail?id=" + userId, "/roles", "/roles/new", "/roles/edit?id=" + roleId,
                "/roles/detail?id=" + roleId)) {
            String html = mvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(html).contains("Rafael Vega Librero").doesNotContain(user("admin").getPasswordHash());
        }
        assertThat(encoder.matches("RvlDemo123!", user("admin").getPasswordHash())).isTrue();
        mvc.perform(get("/users/detail").param("id", "999999")).andExpect(status().isNotFound());
        mvc.perform(get("/roles/edit").param("id", "999999")).andExpect(status().isNotFound());
    }

    @Test
    void completeCrudPersistsSharedRolesAndCleansOnlyDeletedUserAssignments() throws Exception {
        mvc.perform(post("/roles/insert").param("name", "SOPORTE")).andExpect(redirectedUrl("/roles"));
        Long shared = roleId("SOPORTE");
        Long normal = roleId("USER");
        mvc.perform(userRequest("/users/insert", "nuevo_rvl", shared, normal)).andExpect(redirectedUrl("/users"));
        mvc.perform(userRequest("/users/insert", "otro_rvl", shared)).andExpect(redirectedUrl("/users"));
        User first = user("nuevo_rvl");
        User second = user("otro_rvl");
        assertThat(first.getRoles()).extracting("id").containsExactlyInAnyOrder(shared, normal);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_roles WHERE role_id=?", Integer.class, shared)).isEqualTo(2);
        assertThat(encoder.matches("NuevaClave123!", first.getPasswordHash())).isTrue();
        assertThat(first.getPasswordExpiresAt()).isEqualTo(LocalDateTime.of(2027, 1, 27, 10, 0));
        var edit = mvc.perform(get("/users/edit").param("id", first.getId().toString())).andReturn();
        assertThat(((UserForm) edit.getModelAndView().getModel().get("userForm")).getRoleIds())
                .containsExactlyInAnyOrder(shared, normal);
        mvc.perform(userRequest("/users/update", "nuevo_rvl_editado", normal)
                .param("id", first.getId().toString())).andExpect(redirectedUrl("/users"));
        assertThat(user("nuevo_rvl_editado").getRoles()).extracting("id").containsExactly(normal);
        assertThat(user("otro_rvl").getRoles()).extracting("id").containsExactly(shared);
        mvc.perform(post("/roles/update").param("id", shared.toString()).param("name", "SOPORTE_TECNICO"))
                .andExpect(redirectedUrl("/roles"));
        assertThat(user("otro_rvl").getRoles()).extracting("name").containsExactly("SOPORTE_TECNICO");
        mvc.perform(post("/users/delete").param("id", second.getId().toString())).andExpect(redirectedUrl("/users"));
        assertThat(users.findById(second.getId())).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_roles WHERE user_id=?", Integer.class, second.getId())).isZero();
        assertThat(roles.findById(shared)).isPresent();
        mvc.perform(post("/roles/delete").param("id", shared.toString())).andExpect(redirectedUrl("/roles"));
        assertThat(roles.findById(shared)).isEmpty();
        mvc.perform(post("/users/delete").param("id", first.getId().toString())).andExpect(redirectedUrl("/users"));
    }

    @Test
    void blankEditPasswordPreservesHashAndCheckboxesCanBeCleared() throws Exception {
        User existing = user("admin");
        String hash = existing.getPasswordHash();
        mvc.perform(post("/users/update").param("id", existing.getId().toString())
                .param("username", "admin").param("password", "")
                .param("lastPasswordChange", "2026-10-27T10:00").param("failedLoginAttempts", "2")
                .param("_active", "on").param("_accountNonLocked", "on")
                .param("_emailVerified", "on").param("_mustChangePassword", "on")
                .param("roleIds", roleId("ADMIN").toString())).andExpect(redirectedUrl("/users"));
        User updated = user("admin");
        assertThat(updated.getPasswordHash()).isEqualTo(hash);
        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getAccountNonLocked()).isFalse();
        assertThat(updated.getEmailVerified()).isFalse();
        assertThat(updated.getFailedLoginAttempts()).isEqualTo(2);
    }

    @Test
    void userMustHaveAtLeastOneExistingRoleAndFailedWritesLeaveNoPartialUser() throws Exception {
        int count = users.findAll().size();
        mvc.perform(userRequest("/users/insert", "sin_roles")).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userForm", "roleIds"));
        mvc.perform(userRequest("/users/insert", "roles_invalidos", roleId("USER"), 999999L))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("userForm", "roleIds"))
                .andExpect(content().string(containsString("no existe")));
        mvc.perform(userRequest("/users/insert", "rol_no_numerico", roleId("USER"))
                .param("roleIds", "not-a-number")).andExpect(status().isOk())
                .andExpect(model().hasErrors());
        assertThat(users.findAll()).hasSize(count);
        User existing = user("admin");
        var before = existing.getRoles().stream().map(role -> role.getId()).toList();
        mvc.perform(userRequest("/users/update", "admin").param("id", existing.getId().toString()))
                .andExpect(model().attributeHasFieldErrors("userForm", "roleIds"));
        assertThat(user("admin").getRoles()).extracting("id").containsExactlyElementsOf(before);
    }

    @Test
    void validatesRequiredFieldsDatesCountersAndPasswords() throws Exception {
        mvc.perform(post("/users/insert").param("username", " ").param("password", "")
                .param("lastPasswordChange", "").param("failedLoginAttempts", "-1"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("userForm",
                        "username", "password", "lastPasswordChange", "failedLoginAttempts", "roleIds"));
        mvc.perform(replace(userRequest("/users/insert", "clave_corta", roleId("USER")), "password", "123"))
                .andExpect(model().attributeHasFieldErrors("userForm", "password"));
        mvc.perform(replace(userRequest("/users/insert", "unicode_password", roleId("USER")), "password", "á".repeat(40)))
                .andExpect(model().attributeHasFieldErrors("userForm", "password"));
        mvc.perform(replace(replace(userRequest("/users/insert", "fecha_invalida", roleId("USER")),
                "lastPasswordChange", "no-es-fecha"), "failedLoginAttempts", "abc"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("userForm",
                        "lastPasswordChange", "failedLoginAttempts"));
    }

    @Test
    void duplicateNamesAreRejectedWithoutLosingSelectedRoles() throws Exception {
        var response = mvc.perform(userRequest("/users/insert", " ADMIN ", roleId("USER")))
                .andExpect(model().attributeHasFieldErrors("userForm", "username")).andReturn();
        var form = (UserForm) response.getModelAndView().getModel().get("userForm");
        assertThat(form.getRoleIds()).containsExactly(roleId("USER"));
        mvc.perform(post("/roles/insert").param("name", " admin "))
                .andExpect(model().attributeHasFieldErrors("role", "name"));
        mvc.perform(post("/roles/insert").param("name", "  "))
                .andExpect(model().attributeHasFieldErrors("role", "name"));
        mvc.perform(post("/roles/insert").param("name", "a".repeat(51)))
                .andExpect(model().attributeHasFieldErrors("role", "name"));
    }

    @Test
    void roleCrudCannotAssignUsersEvenWhenCraftedFieldsArePosted() throws Exception {
        int assignments = jdbc.queryForObject("SELECT COUNT(*) FROM user_roles", Integer.class);
        mvc.perform(post("/roles/insert").param("name", "NUEVO_ROL").param("userIds", user("admin").getId().toString())
                .param("users[0].id", user("maria").getId().toString())).andExpect(redirectedUrl("/roles"));
        Long id = roleId("NUEVO_ROL");
        mvc.perform(post("/roles/update").param("id", id.toString()).param("name", "ROL_EDITADO")
                .param("userIds", user("admin").getId().toString())).andExpect(redirectedUrl("/roles"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_roles", Integer.class)).isEqualTo(assignments);
        assertThat(roles.isAssigned(id)).isFalse();
        for (String path : List.of("/roles/new", "/roles/edit?id=" + id, "/roles/detail?id=" + id)) {
            String html = mvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            String main = html.substring(html.indexOf("<main"), html.indexOf("</main>"));
            assertThat(main).doesNotContain("name=\"userIds\"", "name=\"users\"", "type=\"checkbox\"", "<select");
        }
    }

    @Test
    void assignedRoleCannotBeDeletedAndUserRetainsItsRoles() throws Exception {
        Long id = roleId("USER");
        int assignments = jdbc.queryForObject("SELECT COUNT(*) FROM user_roles", Integer.class);
        mvc.perform(post("/roles/delete").param("id", id.toString()))
                .andExpect(redirectedUrl("/roles")).andExpect(flash().attributeExists("errorMessage"));
        assertThat(roles.findById(id)).isPresent();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM user_roles", Integer.class)).isEqualTo(assignments);
    }

    @Test
    void translatesScreensAndValidationMessages() throws Exception {
        mvc.perform(get("/users").param("lang", "en")).andExpect(status().isOk())
                .andExpect(content().string(containsString("User list")));
        mvc.perform(post("/roles/insert").param("lang", "en").param("name", " "))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Role name is required.")));
        mvc.perform(userRequest("/users/insert", "english_invalid", 999999L).param("lang", "en"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("does not exist")));
    }
}
