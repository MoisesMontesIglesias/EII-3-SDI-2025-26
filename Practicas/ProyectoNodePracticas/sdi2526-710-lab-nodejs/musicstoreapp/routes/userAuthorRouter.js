const express = require('express');
const path = require("path");
const {ObjectId} = require("mongodb");
const songsRepository = require("../repositories/songsRepository");
const userAuthorRouter = express.Router();
userAuthorRouter.use(function (req, res, next) {
    console.log("userAuthorRouter");
    let songId = path.basename(req.originalUrl);
    if (!ObjectId.isValid(songId)) {
        res.redirectError("El ID de la cancion no es valido", 400, "alert-warning");
        return;
    }
    let filter = {_id: new ObjectId(songId)};
    songsRepository.findSong(filter, {}).then(song => {
        if (song == null) {
            res.redirectError("La cancion no existe", 404, "alert-warning");
            return;
        }
        if (req.session.user && song.author === req.session.user) {
            next();
        } else {
            res.redirectError("No tienes permiso para realizar esta accion", 403, "alert-warning");
        }
    }).catch(error => {
        res.redirectError("Se ha producido un error al validar el autor " + error, 500, "alert-danger");
    });
});
module.exports = userAuthorRouter;
