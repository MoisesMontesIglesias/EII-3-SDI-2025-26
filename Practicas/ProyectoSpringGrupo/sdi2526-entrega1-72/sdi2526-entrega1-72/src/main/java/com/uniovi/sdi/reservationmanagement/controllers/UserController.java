package com.uniovi.sdi.reservationmanagement.controllers;

import com.uniovi.sdi.reservationmanagement.entities.ChangePassword;
import com.uniovi.sdi.reservationmanagement.entities.User;
import com.uniovi.sdi.reservationmanagement.services.UserService;
import com.uniovi.sdi.reservationmanagement.validators.ChangePasswordValidator;
import com.uniovi.sdi.reservationmanagement.validators.UserLoginValidator;
import com.uniovi.sdi.reservationmanagement.validators.UserSignUpValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final UserSignUpValidator userSignUpValidator;
    private final UserLoginValidator userLoginValidator;
    private final ChangePasswordValidator changePasswordValidator;

    /**
     * Crea el controlador de usuarios con sus dependencias.
     *
     * @param userService servicio de usuarios
     * @param userSignUpValidator validador de registro
     * @param userLoginValidator validador de login
     */
    public UserController(
            UserService userService,
            UserSignUpValidator userSignUpValidator,
            UserLoginValidator userLoginValidator,
            ChangePasswordValidator changePasswordValidator
    ) {
        this.userService = userService;
        this.userSignUpValidator = userSignUpValidator;
        this.userLoginValidator = userLoginValidator;
        this.changePasswordValidator = changePasswordValidator;
    }

    /**
     * Muestra el formulario de registro si el usuario no esta autenticado.
     *
     * @param session sesion HTTP actual
     * @param model modelo de vista
     * @return vista de registro o redireccion segun el rol
     */
    @GetMapping("/signup")
    public String showSignUp(HttpSession session, Model model) {
        if (isAuthenticated(session)) {
            return redirectByRole(session);
        }
        model.addAttribute("user", new User());
        return "user/signup";
    }

    /**
     * Procesa el alta de un usuario estandar.
     *
     * @param user datos del formulario
     * @param result resultado de validacion
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de registro con errores o redireccion a espacios
     */
    @PostMapping("/signup")
    public String signUp(
            @ModelAttribute("user") User user,
            BindingResult result,
            HttpSession session,
            HttpServletResponse response,
            Model model
    ) {
        userSignUpValidator.validate(user, result);
        if (result.hasErrors()) {
            return "user/signup";
        }
        try {
            User savedUser = userService.registerStandardUser(
                    new User(user.getDni(), user.getName(), user.getLastName(), user.getPassword())
            );
            session.setAttribute("authenticated", true);
            session.setAttribute("dni", savedUser.getDni());
            session.setAttribute("role", savedUser.getRole());
            setSecurityAuthentication(savedUser);
            return redirectByRole(session);
        } catch (RuntimeException ex) {
            model.addAttribute("signupError", true);
            return "user/signup";
        }
    }

    /**
     * Muestra el formulario de login si no hay sesion valida.
     *
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de login o redireccion segun el rol
     */
    @GetMapping("/login")
    public String showLogin(HttpSession session, Model model) {
        if (isAuthenticated(session)) {
            return redirectByRole(session);
        }
        model.addAttribute("user", new User());
        return "user/login";
    }

    /**
     * Muestra la portada publica o redirige si ya esta autenticado.
     *
     * @param session sesion HTTP
     * @return vista de home o redireccion segun el rol
     */
    @GetMapping("/")
    public String showHome(HttpSession session) {
        return isAuthenticated(session) ? redirectByRole(session) : "home";
    }

    /**
     * Procesa el login y establece la autenticacion en sesion y contexto.
     *
     * @param userForm formulario de login
     * @param result resultado de validacion
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de login con errores o redireccion a espacios
     */
    @PostMapping("/login")
    public String login(
            @ModelAttribute("user") User userForm,
            BindingResult result,
            HttpSession session,
            HttpServletResponse response,
            Model model
    ) {
        userLoginValidator.validate(userForm, result);
        if (result.hasErrors()) {
            return "user/login";
        }

        if (!userService.authenticate(userForm.getDni(), userForm.getPassword())) {
            model.addAttribute("loginError", true);
            return "user/login";
        }

        User loggedUser = userService.findByDni(userForm.getDni()).orElseThrow();
        session.setAttribute("authenticated", true);
        session.setAttribute("dni", loggedUser.getDni());
        session.setAttribute("role", loggedUser.getRole());
        setSecurityAuthentication(loggedUser);
        return redirectByRole(session);
    }

    /**
     * Muestra el formulario de cambio de contrasena.
     *
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de cambio de contrasena o redireccion a login
     */
    @GetMapping("/password/change")
    public String showChangePassword(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        model.addAttribute("changePassword", new ChangePassword());
        return "user/change-password";
    }

    /**
     * Procesa el cambio de contrasena.
     *
     * @param form formulario de cambio
     * @param result resultado de validacion
     * @param session sesion HTTP
     * @param model modelo de vista
     * @return vista de cambio con mensajes de error o exito
     */
    @PostMapping("/password/change")
    public String changePassword(
            @ModelAttribute("changePassword") ChangePassword form,
            BindingResult result,
            HttpSession session,
            Model model
    ) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        changePasswordValidator.validate(form, result);
        if (result.hasErrors()) {
            return "user/change-password";
        }

        String dni = String.valueOf(session.getAttribute("dni"));
        if (!userService.authenticate(dni, form.getCurrentPassword())) {
            model.addAttribute("changeError", "La contraseña actual no es correcta.");
            return "user/change-password";
        }

        userService.updatePassword(dni, form.getNewPassword());
        model.addAttribute("changeSuccess", "Contraseña actualizada.");
        return "user/change-password";
    }

    /**
     * Muestra el perfil del usuario autenticado.
     *
     * @param session sesion HTTP
     * @param model modelo de vista
     * @param from origen opcional para resolver la ruta de retorno
     * @return vista de perfil o redireccion a login si no hay sesion valida
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model, @RequestParam(required = false) String from) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        String dni = String.valueOf(session.getAttribute("dni"));
        User user = userService.findByDni(dni).orElseThrow();
        model.addAttribute("profileUser", user);
        model.addAttribute("backPath", resolveBackPath(from, session));
        return "user/profile";
    }

    /**
     * Determina si la sesion y el contexto de seguridad son validos.
     *
     * @param session sesion HTTP
     * @return true si hay autenticacion valida
     */
    private boolean isAuthenticated(HttpSession session) {
        Object authenticated = session.getAttribute("authenticated");
        if (!(authenticated instanceof Boolean) || !((Boolean) authenticated)) {
            return false;
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            clearSessionAuthentication(session);
            return false;
        }
        boolean hasAppRole = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                                || "ROLE_USER".equals(authority.getAuthority()));
        if (!hasAppRole) {
            clearSessionAuthentication(session);
            return false;
        }
        Object role = session.getAttribute("role");
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            clearSessionAuthentication(session);
            return false;
        }
        return true;
    }

    /**
     * Devuelve la redireccion adecuada segun rol.
     *
     * @param session sesion HTTP
     * @return ruta de redireccion
     */
    @SuppressWarnings("unused")
    private String redirectByRole(HttpSession session) {
        Object role = session.getAttribute("role");
        if ("ADMIN".equals(role)) {
            return "redirect:/reservas/listado-global";
        }
        return "redirect:/spaces";
    }

    /**
     * Registra la autenticacion en el contexto de Spring Security.
     *
     * @param user usuario autenticado
     */
    private void setSecurityAuthentication(User user) {
        String role = user.getRole();
        var authorities = "ADMIN".equals(role)
                ? java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken(user.getDni(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Limpia la sesion y el contexto de seguridad.
     *
     * @param session sesion HTTP
     */
    private void clearSessionAuthentication(HttpSession session) {
        session.removeAttribute("authenticated");
        session.removeAttribute("dni");
        session.removeAttribute("role");
        SecurityContextHolder.clearContext();
    }

    private String resolveBackPath(String from, HttpSession session) {
        if ("reservations".equals(from) && "USER".equals(session.getAttribute("role"))) {
            return "/reservas/mis";
        }
        if ("global".equals(from) && "ADMIN".equals(session.getAttribute("role"))) {
            return "/reservas/listado-global";
        }
        return "/spaces";
    }
}
