module.exports = function (app) {
    app.get("/authors/add", function (req, res) {
        let roles = [
            "cantante",
            "trompetista",
            "violinista",
            "saxofonista",
            "pianista"
        ];
        res.render("authors/add.twig", { roles: roles });
    });

    app.post("/authors/add", function (req, res) {
        let missing = [];
        if (req.body.name === undefined || req.body.name === null) {
            missing.push("name");
        }
        if (req.body.group === undefined || req.body.group === null) {
            missing.push("group");
        }
        if (req.body.rol === undefined || req.body.rol === null) {
            missing.push("rol");
        }

        if (missing.length > 0) {
            let response = missing.map(function (param) {
                return param + " no enviado en la peticion.";
            }).join("<br>");
            res.redirectError(response, 400);
            return;
        }

        let response = "Autor agregado: " + req.body.name + "<br>"
            + " grupo: " + req.body.group + "<br>"
            + " rol: " + req.body.rol;
        res.redirectError(response, 200, "alert-info");
    });

    function getAuthors() {
        return [{
            "name": "Freddie Mercury",
            "group": "Queen",
            "rol": "cantante"
        }, {
            "name": "Dizzy Gillespie",
            "group": "All-Star",
            "rol": "trompetista"
        }, {
            "name": "Itzhak Perlman",
            "group": "Solo",
            "rol": "violinista"
        }, {
            "name": "John Coltrane",
            "group": "Classic Quartet",
            "rol": "saxofonista"
        }, {
            "name": "Martha Argerich",
            "group": "Solo",
            "rol": "pianista"
        }];
    }

    app.get("/authors", function (req, res) {
        res.render("authors/authors.twig", { authors: getAuthors() });
    });

    app.get("/authors/filter/:rol", function (req, res) {
        let authors = getAuthors().filter(function (author) {
            return author.rol === req.params.rol;
        });
        res.render("authors/authors.twig", { authors: authors });
    });

    app.get(["/authors/*", "/author*"], function (req, res) {
        res.redirect("/authors");
    });
};
