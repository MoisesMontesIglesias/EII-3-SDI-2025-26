document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-password-toggle]").forEach(function (button) {
        var input = button.parentElement.querySelector("input");
        if (!input) {
            return;
        }

        var showLabel = button.getAttribute("aria-label") || "Mostrar contrasena";
        var hideLabel = document.documentElement.lang === "en" ? "Hide password" : "Ocultar contrasena";

        button.addEventListener("click", function () {
            var showing = input.type === "text";
            input.type = showing ? "password" : "text";
            button.setAttribute("aria-pressed", String(!showing));
            button.setAttribute("aria-label", showing ? showLabel : hideLabel);
            button.setAttribute("title", showing ? showLabel : hideLabel);
        });
    });
});
