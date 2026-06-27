const {ObjectId} = require("mongodb");
module.exports = function (app, songsRepository, usersRepository) {
    /**
     * @swagger
     * /api/v1.0/songs:
     *   get:
     *     summary: Obtener lista de canciones
     *     description: Retorna todas las canciones almacenadas en el sistema. Puede filtrarse opcionalmente por texto de busqueda.
     *     tags:
     *       - Songs
     *     responses:
     *       200:
     *         description: Lista de canciones obtenida correctamente.
     *         content:
     *           application/json:
     *             schema:
     *               type: object
     *               properties:
     *                 songs:
     *                   type: array
     *                   items:
     *                     $ref: '#/components/schemas/Song'
     *       500:
     *         description: Error interno del servidor al recuperar las canciones.
     *         content:
     *           application/json:
     *             schema:
     *               type: object
     *               properties:
     *                 error:
     *                   type: string
     *                   example: Se ha producido un error al recuperar las canciones.
     */
    app.get("/api/v1.0/songs", function (req, res) {
        let filter = {};
        let options = {};
        songsRepository.getSongs(filter, options).then(songs => {
            res.status(200).json({songs: songs})
        }).catch(() => {
            res.status(500).json({ error: "Se ha producido un error al recuperar las canciones." })
        });
    });

    app.get("/api/v1.0/songs/:id", function (req, res) {
        try {
            let songId = new ObjectId(req.params.id)
            let filter = {_id: songId};
            let options = {};
            songsRepository.findSong(filter, options).then(song => {
                if (song === null) {
                    res.status(404).json({error: "ID inválido o no existe"})
                } else {
                    res.status(200).json({song: song})
                }
            }).catch(error => {
                res.status(500).json({error: "Se ha producido un error al recuperar la canción."})
            });
        } catch (error) {
            res.status(500).json({error: "Se ha producido un error :" + error.message})
        }
    });
    /**
     * @swagger
     * /api/v1.0/songs/{id}:
     *   delete:
     *     summary: Eliminar una canción
     *     description: Elimina una canción del sistema a partir de su identificador.
     *     tags:
     *       - Songs
     *     parameters:
     *       - in: path
     *         name: id
     *         required: true
     *         description: Identificador único de la canción.
     *         schema:
     *           type: string
     *     responses:
     *       200:
     *         description: Canción eliminada correctamente.
     *
     *       404:
     *         description: ID inválido o canción no encontrada.
     *
     *       500:
     *         description: Error interno del servidor.
     *
     */
    app.delete('/api/v1.0/songs/:id', function (req, res) {
        try {
            let songId = new ObjectId(req.params.id)
            let filter = {_id: songId}
            songsRepository.deleteSong(filter, {}).then(result => {
                if (result === null || result.deletedCount === 0) {
                    res.status(404).json({error: "ID inválido o no existe, no se ha borrado el registro."});
                } else {
                    res.status(200).send(JSON.stringify(result));
                }
            }).catch(error => {
                res.status(500).json({error: "Se ha producido un error:" + error.message})
            });
        } catch (error) {
            res.status(500). json({error: error.message})
        }
    });
    /**
     * @swagger
     * /api/v1.0/songs:
     *   post:
     *     summary: Crear una nueva canción
     *     description: Añade una nueva canción al sistema.
     *     tags:
     *       - Songs
     *     requestBody:
     *       required: true
     *       content:
     *         application/json:
     *           schema:
     *            $ref: '#/components/schemas/SongRequest'
     *     responses:
     *       201:
     *         description: Canción creada correctamente.
     *         content:
     *           application/json:
     *             schema:
     *               type: object
     *               properties:
     *                 message:
     *                   type: string
     *                   example: Canción añadida correctamente.
     *                 _id:
     *                   type: string
     *       409:
     *         description: Conflicto, la canción ya existe.
     *         content:
     *           application/json:
     *             schema:
     *               type: object
     *               properties:
     *                 error:
     *                   type: string
     *                   example: No se ha podido crear la canción. El recurso ya existe.
     *       500:
     *         description: Error interno del servidor.
     *         content:
     *           application/json:
     *             schema:
     *               type: object
     *               properties:
     *                 error:
     *                   type: string
     */
    app.post('/api/v1.0/songs', function (req, res) {
        try {
            let song = {
                title: req.body.title,
                kind: req.body.kind,
                price: req.body.price,
                author: res.user
            }
            songsRepository.insertSong(song, function (createdSong) {
                if (createdSong == null || createdSong.error != null) {
                    res.status(409).json({error: "No se ha podido crear la canción. El recurso ya existe."});
                } else {
                    const createdSongId = createdSong.songId;
                    if (req.files != null) {
                        let image = req.files.cover;
                        if (image == null) {
                            res.status(400).json({error: "La portada de la cancion es obligatoria"});
                            return;
                        }

                        image.mv(req.app.get("uploadPath") + '/public/covers/' + createdSongId + '.png')
                            .then(() => {
                                if (req.files.audio != null) {
                                    let audio = req.files.audio;
                                    audio.mv(req.app.get("uploadPath") + '/public/audios/' + createdSongId + '.mp3')
                                        .then(() => {
                                            res.status(201).json({
                                                message: "Canción añadida correctamente.",
                                                _id: createdSongId
                                            });
                                        })
                                        .catch(error => {
                                            res.status(500).json({error: "Error al subir el audio de la cancion: " + error.message});
                                        });
                                } else {
                                    res.status(201).json({
                                        message: "Canción añadida correctamente.",
                                        _id: createdSongId
                                    });
                                }
                            })
                            .catch(error => {
                                res.status(500).json({error: "Error al subir la portada de la cancion: " + error.message});
                            });
                    } else {
                        res.status(201).json({
                            message: "Canción añadida correctamente.",
                            _id: createdSongId
                        })
                    }
                }
            });
        } catch (error) {
            res.status(500).json({error: "Se ha producido un error al intentar crear la canción: " +
                    error.message})
        }
    }) ;

    /**
     * @swagger
     * /api/v1.0/songs/{id}:
     *   put:
     *     summary: Modificar una cancion
     *     description: Actualiza los datos de una cancion existente mediante su identificador.
     *     tags:
     *       - Songs
     *     parameters:
     *       - in: path
     *         name: id
     *         required: true
     *         description: Identificador unico de la cancion.
     *         schema:
     *           type: string
     *     requestBody:
     *       required: true
     *       content:
     *         application/json:
     *           schema:
     *             $ref: '#/components/schemas/SongRequest'
     *     responses:
     *       200:
     *         description: Cancion modificada correctamente.
     *       404:
     *         description: ID invalido o cancion no encontrada.
     *       409:
     *         description: No se ha realizado ninguna modificacion.
     *       500:
     *         description: Error interno del servidor.
     */
    app.put('/api/v1.0/songs/:id', function (req, res) {
        try {
            let songId = new ObjectId(req.params.id);
            let filter = {_id: songId};
//Si la _id NO no existe, no crea un nuevo documento.
            const options = {upsert: false};
            let song = {
                author: req.res.user
            }
            if (typeof req.body.title !== "undefined" && req.body.title !== null)
                song.title = req.body.title;
            if (typeof req.body.kind !== "undefined" &&  req.body.kind !== null)
                song.kind = req.body.kind;
            if (typeof req.body.price !== "undefined" &&  req.body.price !== null)
                song.price = req.body.price;

            songsRepository.updateSong(song, filter, options).then(result => {
                if (result === null) {
                    res.status(404).json({error: "ID inválido o no existe, no se ha actualizado la canción."});
                }
                //La _id No existe o los datos enviados no difieren de los ya almacenados.
                else if (result.modifiedCount === 0) {
                    res.status(409).json({error: "No se ha modificado ninguna canción."});
                }
                else{
                    res.status(200).json({
                        message: "Canción modificada correctamente.",
                        result: result
                    })
                }
            }).catch(error => {
                res.status(500)
                    .json({error : "Se ha producido un error al modificar la canción: "+ error.message})
            });
        } catch (error) {
            res.status(500)
                .json({error: "Se ha producido un error al intentar modificar la canción: "+ error.message})
        }
    });
    app.post('/api/v1.0/users/login', async function (req, res) {
        try {
            const securePassword = app.get("crypto").createHmac('sha256', app.get('clave'))
                .update(req.body.password).digest('hex');

            const filter = {
                email: req.body.email,
                password: securePassword
            };

            const user = await usersRepository.findUser(filter, {});

            if (user == null) {
                return res.status(401).json({
                    message: "usuario no autorizado",
                    authenticated: false });
            }

            const token = app.get('jwt').sign(
                { user: user.email, time: Date.now() / 1000 },
                "secreto"
            );

            res.status(200).json({
                message: "usuario autorizado",
                authenticated: true,
                token: token });

        } catch (e) {
            res.status(500).json({
                message: "Se ha producido un error al verificar credenciales",
                authenticated: false });
        }
    });
}
