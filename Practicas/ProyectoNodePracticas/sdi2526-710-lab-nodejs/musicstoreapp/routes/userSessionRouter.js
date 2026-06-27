const express = require('express');
const userSessionRouter = express.Router();

userSessionRouter.use(function(req, res, next) {
    console.log("routerUsuarioSession");
    if (req.session.user) {
        // dejamos correr la peticion
        next();
    } else {
        console.log("va a: " + req.originalUrl);
        req.session.returnTo = req.originalUrl;
        res.redirect("/users/login?message=" + encodeURIComponent("Debes iniciar sesion para continuar") +
            "&messageType=" + encodeURIComponent("alert-warning"));
    }
});

module.exports = userSessionRouter;
