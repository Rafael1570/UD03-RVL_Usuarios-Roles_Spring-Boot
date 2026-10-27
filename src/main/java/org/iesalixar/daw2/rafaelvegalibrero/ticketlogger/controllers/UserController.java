package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.controllers;

import java.util.Locale;
import jakarta.validation.Valid;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.forms.UserForm;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services.FormException;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services.RoleService;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService users;
    private final RoleService roles;
    private final MessageSource messages;

    public UserController(UserService users, RoleService roles, MessageSource messages) {
        this.users = users;
        this.roles = roles;
        this.messages = messages;
    }
    @InitBinder("userForm")
    public void configureBinding(WebDataBinder binder) {
        binder.setAllowedFields("id", "username", "password", "active", "accountNonLocked",
                "lastPasswordChange", "failedLoginAttempts", "emailVerified", "mustChangePassword", "roleIds");
    }
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", users.findAll());
        return "views/user/user-list";
    }
    @GetMapping("/detail")
    public String detail(@RequestParam Long id, Model model) {
        model.addAttribute("user", users.findById(id));
        return "views/user/user-detail";
    }
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return form(model);
    }
    @GetMapping("/edit")
    public String editForm(@RequestParam Long id, Model model) {
        model.addAttribute("userForm", users.toForm(users.findById(id)));
        return form(model);
    }
    private String form(Model model) {
        model.addAttribute("availableRoles", roles.findAll());
        return "views/user/user-form";
    }
    @PostMapping("/insert")
    public String insert(@Validated(UserForm.Create.class) @ModelAttribute("userForm") UserForm userForm,
                         BindingResult errors, Model model, RedirectAttributes flash, Locale locale) {
        userForm.setId(null);
        if (errors.hasErrors()) return form(model);
        try {
            users.create(userForm);
            flash.addFlashAttribute("successMessage", messages.getMessage("users.created", null, locale));
            return "redirect:/users";
        } catch (FormException exception) {
            errors.rejectValue(exception.getField(), exception.getCode());
        } catch (DuplicateKeyException exception) {
            errors.rejectValue("username", "validation.user.duplicate");
        } catch (DataAccessException exception) {
            logger.error("Error al crear el usuario", exception);
            errors.reject("error.database");
        }
        return form(model);
    }
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("userForm") UserForm userForm, BindingResult errors,
                         Model model, RedirectAttributes flash, Locale locale) {
        users.findById(userForm.getId());
        if (errors.hasErrors()) return form(model);
        try {
            users.update(userForm);
            flash.addFlashAttribute("successMessage", messages.getMessage("users.updated", null, locale));
            return "redirect:/users";
        } catch (FormException exception) {
            errors.rejectValue(exception.getField(), exception.getCode());
        } catch (DuplicateKeyException exception) {
            errors.rejectValue("username", "validation.user.duplicate");
        } catch (DataAccessException exception) {
            logger.error("Error al actualizar el usuario", exception);
            errors.reject("error.database");
        }
        return form(model);
    }
    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes flash, Locale locale) {
        users.delete(id);
        flash.addFlashAttribute("successMessage", messages.getMessage("users.deleted", null, locale));
        return "redirect:/users";
    }
}
