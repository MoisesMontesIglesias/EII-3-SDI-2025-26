module.exports = function (app, usersRepository) {
  function redirectToLogin(res, message) {
    res.redirect("/users/login?message=" + encodeURIComponent(message) +
      "&messageType=" + encodeURIComponent("alert-warning"));
  }

  app.get('/users', function (req, res) {
    res.send('lista de usuarios');
  });

  app.get('/users/signup', function (req, res) {
    res.render("signup.twig");
  });

  app.post('/users/signup', function (req, res) {
    if (!req.body.email || !req.body.password) {
      res.redirectError("Faltan datos de registro", 400);
      return;
    }

    let securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
        .update(req.body.password).digest('hex');
    let user = {
      email: req.body.email,
      password: securePassword
    };
    usersRepository.insertUser(user).then(userId => {
      res.redirectError("Nuevo usuario registrado.", 200, "alert-info");
    }).catch(error => {
      res.redirectError("Se ha producido un error al registrar el usuario.", 500);
    });
  });

  app.get('/users/login', function (req, res) {
    res.render("login.twig");
  });

  app.get('/users/logout', function (req, res) {
    req.session.user = null;
    res.redirectError("El usuario se ha desconectado correctamente", 200, "alert-info");
  });

  app.post('/users/login', function (req, res) {
    if (!req.body.email || !req.body.password) {
      redirectToLogin(res, "Email o password incorrecto");
      return;
    }

    let securePassword = app.get("crypto")
      .createHmac('sha256', app.get('clave'))
      .update(req.body.password).digest('hex');
    let filter = {
      email: req.body.email,
      password: securePassword
    };
    let options = {};
    usersRepository.findUser(filter, options).then(user => {
      if (user == null) {
        req.session.user = null;
        redirectToLogin(res, "Email o password incorrecto");
      } else {
        req.session.user = user.email;
        let returnTo = req.session.returnTo;
        req.session.returnTo = null;
        res.redirect(returnTo || "/publications");
      }
    }).catch(error => {
      req.session.user = null;
      res.redirectError("Se ha producido un error al buscar el usuario", 500);
    });
  });
};
