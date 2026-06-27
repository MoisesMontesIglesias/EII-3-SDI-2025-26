const { ObjectId } = require("mongodb");

module.exports = function (app, songsRepository) {
    app.get('/add', function (req, res) {
        let response = parseInt(req.query.num1) + parseInt(req.query.num2);
        res.send(String(response));
    });

    app.get('/songs/add', function (req, res) {
        res.render("songs/add.twig");
    });

    app.get('/purchases', function (req, res) {
        let filter = { user: req.session.user };
        let options = { projection: { _id: 0, song_id: 1 } };
        songsRepository.getPurchases(filter, options).then(purchasedIds => {
            const purchasedSongs = purchasedIds.map(song => song.song_id);
            let filter = { "_id": { $in: purchasedSongs } };
            let options = { sort: { title: 1 } };
            songsRepository.getSongs(filter, options).then(songs => {
                res.render("purchase.twig", { songs: songs });
            }).catch(error => {
                res.redirectError("Se ha producido un error al listar las publicaciones del usuario: " + error, 500);
            });
        }).catch(error => {
            res.redirectError("Se ha producido un error al listar las canciones del usuario " + error, 500);
        });
    });

    app.post('/songs/buy/:id', function (req, res) {
        if (!ObjectId.isValid(req.params.id)) {
            res.redirectError("El ID de la cancion no es valido", 400);
            return;
        }

        let songId = new ObjectId(req.params.id);

        songsRepository.findSong({ _id: songId }, {}).then(song => {
            if (song == null) {
                res.redirectError("La cancion no existe", 404);
                return;
            }

            if (song.author === req.session.user) {
                res.redirectError("El autor no puede comprar su propia cancion", 403);
                return;
            }

            canPurchaseSong(req.session.user, songId, purchases => {
                if (purchases.length > 0) {
                    res.redirectError("La cancion ya ha sido comprada", 400);
                    return;
                }

                songsRepository.buySong({
                    user: req.session.user,
                    song_id: songId
                }).then(result => {
                    if (result.insertedId === null || typeof (result.insertedId) === undefined) {
                        res.redirectError("Se ha producido un error al comprar la cancion", 500);
                    } else {
                        res.redirect("/purchases");
                    }
                }).catch(error => {
                    res.redirectError("Se ha producido un error al comprar la cancion " + error, 500);
                });
            });
        }).catch(error => {
            res.redirectError("Se ha producido un error al buscar la cancion " + error, 500);
        });
    });

    app.get('/songs/delete/:id', function (req, res) {
        let filter = { _id: new ObjectId(req.params.id) };
        songsRepository.deleteSong(filter, {}).then(result => {
            if (result === null || result.deletedCount === 0) {
                res.redirectError("No se ha podido eliminar el registro", 404);
            } else {
                res.redirect("/publications");
            }
        }).catch(error => {
            res.redirectError("Se ha producido un error al intentar eliminar la cancion: " + error, 500);
        });
    });

    app.get('/songs/edit/:id', function (req, res) {
        if (!ObjectId.isValid(req.params.id)) {
            res.redirectError("El ID de la cancion no es valido", 400);
            return;
        }

        let filter = { _id: new ObjectId(req.params.id) };
        songsRepository.findSong(filter, {}).then(song => {
            res.render("songs/edit.twig", { song: song });
        }).catch(error => {
            res.redirectError("Se ha producido un error al recuperar la cancion " + error, 500);
        });
    });

    app.post('/songs/edit/:id', function (req, res) {
        if (!ObjectId.isValid(req.params.id)) {
            res.redirectError("El ID de la cancion no es valido", 400);
            return;
        }

        let song = {
            title: req.body.title,
            kind: req.body.kind,
            price: req.body.price,
            author: req.session.user
        };
        let songId = req.params.id;
        let filter = { _id: ObjectId(songId) };
        const options = { upsert: false };

        songsRepository.updateSong(song, filter, options).then(result => {
            step1UpdateCover(req.files, songId, function (result) {
                if (result == null) {
                    res.redirectError("Error al actualizar la portada o el audio de la cancion", 500);
                } else {
                    res.redirect("/publications");
                }
            });
        }).catch(error => {
            res.redirectError("Se ha producido un error al modificar la cancion " + error, 500);
        });
    });

    app.post('/songs/add', function (req, res) {
        let song = {
            title: req.body.title,
            kind: req.body.kind,
            price: req.body.price,
            author: req.session.user
        };

        songsRepository.insertSong(song, function (result) {
            if (result.songId !== null && result.songId !== undefined) {
                if (req.files != null) {
                    let image = req.files.cover;

                    image.mv(app.get("uploadPath") + '/public/covers/' + result.songId + '.png')
                        .then(() => {
                            if (req.files.audio != null) {
                                let audio = req.files.audio;

                                audio.mv(app.get("uploadPath") + '/public/audios/' + result.songId + '.mp3')
                                    .then(() => res.redirect("/publications"))
                                    .catch(() => res.redirectError("Error al subir el audio de la cancion", 500));
                            } else {
                                res.redirect("/publications");
                            }
                        })
                        .catch(() => res.redirectError("Error al subir la portada de la cancion", 500));
                } else {
                    res.redirect("/publications");
                }
            } else {
                res.redirectError("Error al insertar cancion " + result.error, 500);
            }
        });
    });

    app.get('/shop', function (req, res) {
        let filter = {};
        let options = { sort: { title: 1 } };
        if (req.query.search != null && typeof (req.query.search) != "undefined" && req.query.search != "") {
            filter = { "title": { $regex: ".*" + req.query.search + ".*" } };
        }
        let page = parseInt(req.query.page);
        if (typeof req.query.page === "undefined" || req.query.page === null || req.query.page === "0") {
            page = 1;
        }
        songsRepository.getSongsPg(filter, options, page).then(result => {
            let lastPage = result.total / 4;
            if (result.total % 4 > 0) {
                lastPage = lastPage + 1;
            }
            let pages = [];
            for (let i = page - 2; i <= page + 2; i++) {
                if (i > 0 && i <= lastPage) {
                    pages.push(i);
                }
            }
            let response = {
                songs: result.songs,
                pages: pages,
                currentPage: page
            };
            res.render("shop.twig", response);
        }).catch(error => {
            res.redirectError("Se ha producido un error al listar las canciones del usuario " + error, 500);
        });
    });

    app.get('/songs/:id', function (req, res, next) {
        if (!ObjectId.isValid(req.params.id)) {
            next();
            return;
        }

        let songId = new ObjectId(req.params.id);
        let user = req.session.user;
        let filter = { _id: songId };
        let options = {};

        songsRepository.findSong(filter, options).then(song => {
            if (song == null) {
                res.redirectError("La cancion no existe", 404);
                return;
            }

            canPurchaseSong(user, songId, function (purchases) {
                let canBuySong = user && song.author !== user && purchases.length === 0;
                let settings = {
                    url: "https://api.currencyapi.com/v3/latest?apikey=cur_live_a8krFg7RI7S24XyjcCsShpWEUenbl3EccKDbFMd5&base_currency=EUR&currencies=USD",
                    method: "get",
                };
                let rest = app.get("rest");

                rest(settings, function (error, response, body) {
                    if (error) {
                        res.redirectError("Se ha producido un error al obtener el tipo de cambio " + error, 500);
                        return;
                    }

                    console.log("cod: " + response.statusCode + " Cuerpo :" + body);

                    let responseObject = JSON.parse(body);
                    let rateUSD = responseObject.data.USD.value;
                    let songValue = song.price / rateUSD;
                    song.usd = Math.round(songValue * 100) / 100;

                    let itunesSettings = {
                        url: "https://itunes.apple.com/search?term=" +
                            encodeURIComponent(song.title) +
                            "&country=US&media=music&entity=song&limit=5",
                        method: "get",
                        json: true
                    };

                    rest(itunesSettings, function (error2, response2, body2) {
                        let itunesResults = [];
                        if (!error2 && response2 && response2.statusCode === 200 && body2 && body2.results) {
                            const normalizeText = (text) => {
                                return text
                                    .normalize("NFD")
                                    .replace(/[\u0300-\u036f]/g, "")
                                    .trim()
                                    .toLowerCase();
                            };
                            const songTitle = normalizeText(song.title);
                            itunesResults = body2.results.filter(item => {
                                return item.trackName != null &&
                                    normalizeText(item.trackName) === songTitle;
                            });
                        }

                        res.render("songs/song.twig", {
                            song: song,
                            canBuy: canBuySong,
                            itunesResults: itunesResults
                        });
                    });
                });
            });
        }).catch(error => {
            res.redirectError("Se ha producido un error al buscar la cancion " + error, 500);
        });
    });

    app.get('/promo*', function (req, res) {
        res.send('Respuesta al patron promo*');
    });

    app.get('/pro*ar', function (req, res) {
        res.send('Respuesta al patron pro*ar');
    });

    app.get("/songs", function (req, res) {
        let songs = [
            { title: "Blank space", price: "1.2" },
            { title: "See you again", price: "1.3" },
            { title: "Uptown Funk", price: "1.1" }
        ];

        res.render("shop.twig", {
            seller: "Tienda de canciones",
            songs: songs
        });
    });

    app.get('/publications', function (req, res) {
        let filter = { author: req.session.user };
        let options = { sort: { title: 1 } };
        songsRepository.getSongs(filter, options).then(songs => {
            res.render("publications.twig", { songs: songs });
        }).catch(error => {
            res.redirectError("Se ha producido un error al listar las publicaciones del usuario:" + error, 500);
        });
    });

    app.get('/songs/edit/:id', function (req, res) {
        if (!ObjectId.isValid(req.params.id)) {
            res.redirectError("El ID de la cancion no es valido", 400);
            return;
        }

        let filter = { _id: new ObjectId(req.params.id) };
        songsRepository.findSong(filter, {}).then(song => {
            res.render("songs/edit.twig", { song: song });
        }).catch(error => {
            res.redirectError("Se ha producido un error al recuperar la cancion " + error, 500);
        });
    });

    app.get('/songs/:kind/:id', function (req, res) {
        res.send('id: ' + req.params.id + '<br>Tipo de musica: ' + req.params.kind);
    });

    function step1UpdateCover(files, songId, callback) {
        if (files && files.cover != null) {
            let image = files.cover;
            image.mv(app.get("uploadPath") + '/public/covers/' + songId + '.png', function (err) {
                if (err) {
                    callback(null);
                } else {
                    step2UpdateAudio(files, songId, callback);
                }
            });
        } else {
            step2UpdateAudio(files, songId, callback);
        }
    }

    function step2UpdateAudio(files, songId, callback) {
        if (files && files.audio != null) {
            let audio = files.audio;
            audio.mv(app.get("uploadPath") + '/public/audios/' + songId + '.mp3', function (err) {
                if (err) {
                    callback(null);
                } else {
                    callback(true);
                }
            });
        } else {
            callback(true);
        }
    }

    function canPurchaseSong(user, songId, callback) {
        songsRepository.getPurchases({
            user: user,
            song_id: songId
        }, { projection: { _id: 0, song_id: 1 } }).then(purchases => {
            callback(purchases);
        }).catch(() => {
            callback([]);
        });
    }
};
