package org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.controllers;

import java.util.Locale;
import jakarta.validation.Valid;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.entities.Role;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services.FormException;
import org.iesalixar.daw2.rafaelvegalibrero.ticketlogger.services.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/roles")
public class RoleController {
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    private final RoleService roles;
    private final MessageSource messages;

    public RoleController(RoleService roles, MessageSource messages) {
        this.roles = roles;
        this.messages = messages;
    }
    // Ni el modelo Role ni el binding permiten recibir usuarios o asignaciones.
    @InitBinder("role")
    public void configureBinding(WebDataBinder binder) { binder.setAllowedFields("id", "name"); }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roles.findAll());
        return "views/role/role-list";
    }
    @GetMapping("/detail")
    public String detail(@RequestParam Long id, Model model) {
        model.addAttribute("role", roles.findById(id));
        return "views/role/role-detail";
    }
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("role", new Role());
        return "views/role/role-form";
    }
    @GetMapping("/edit")
    public String editForm(@RequestParam Long id, Model model) {
        model.addAttribute("role", roles.findById(id));
        return "views/role/role-form";
    }
    @PostMapping("/insert")
    public String insert(@Valid @ModelAttribute("role") Role role, BindingResult errors,
                         RedirectAttributes flash, Locale locale) {
        role.setId(null);
        return save(role, errors, flash, locale, false);
    }
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("role") Role role, BindingResult errors,
                         RedirectAttributes flash, Locale locale) {
        roles.findById(role.getId());
        return save(role, errors, flash, locale, true);
    }
    private String save(Role role, BindingResult errors, RedirectAttributes flash, Locale locale, boolean updating) {
        if (errors.hasErrors()) return "views/role/role-form";
        try {
            if (updating) roles.update(role); else roles.create(role);
            flash.addFlashAttribute("successMessage", messages.getMessage(updating ? "roles.updated" : "roles.created", null, locale));
            return "redirect:/roles";
        } catch (FormException exception) {
            errors.rejectValue(exception.getField(), exception.getCode());
        } catch (DuplicateKeyException exception) {
            errors.rejectValue("name", "validation.role.duplicate");
        } catch (DataAccessException exception) {
            logger.error("Error al guardar el rol", exception);
            errors.reject("error.database");
        }
        return "views/role/role-form";
    }
    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes flash, Locale locale) {
        try {
            roles.delete(id);
            flash.addFlashAttribute("successMessage", messages.getMessage("roles.deleted", null, locale));
        } catch (FormException exception) {
            flash.addFlashAttribute("errorMessage", messages.getMessage(exception.getCode(), null, locale));
        } catch (DataIntegrityViolationException exception) {
            flash.addFlashAttribute("errorMessage", messages.getMessage("role.inUse", null, locale));
        }
        return "redirect:/roles";
    }
}
