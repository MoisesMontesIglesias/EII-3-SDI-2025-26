const { ObjectId } = require("mongodb");

module.exports = function (app, songsRepository) {
    app.get('/itunes', function (req, res) {
        const term = (req.query.term || '').trim();
        if (term.length === 0) {
            res.render('itunes/search.twig', {
                term: '',
                results: []
            });
            return;
        }

        const rest = app.get('rest');
        const settings = {
            url: "https://itunes.apple.com/search?term=" +
                encodeURIComponent(term) +
                "&country=US&media=music&entity=song&limit=10",
            method: "get",
            json: true
        };

        rest(settings, function (error, response, body) {
            if (error || !response || response.statusCode !== 200) {
                res.redirectError("Se ha producido un error al consultar iTunes", 500);
                return;
            }

            res.render('itunes/search.twig', {
                term: term,
                results: body && body.results ? body.results.map(function (item) {
                    return Object.assign({}, item, {
                        priceEur: Math.round((3 + Math.random() * 7) * 100) / 100
                    });
                }) : []
            });
        });
    });

    app.get('/itunes/library', function (req, res) {
        const filter = { user: req.session.user, trackId: { $exists: true } };
        const options = { sort: { createdAt: -1 } };

        songsRepository.getSongs(filter, options).then(function (songs) {
            const viewSongs = songs.map(function (song) {
                return Object.assign({}, song, {
                    id: song._id.toString()
                });
            });
            res.render('itunes/library.twig', {
                songs: viewSongs
            });
        }).catch(function (error) {
            res.redirectError("Se ha producido un error al listar las canciones importadas " + error, 500);
        });
    });

    app.post('/itunes/import', function (req, res) {
        const trackId = req.body.trackId;
        const requestedPrice = parseFloat(req.body.priceEur);
        if (trackId == null || trackId === '') {
            res.status(400).json({ error: "Falta el identificador de iTunes" });
            return;
        }

        const duplicateFilter = {
            user: req.session.user,
            trackId: parseInt(trackId)
        };

        songsRepository.findSong(duplicateFilter, {}).then(function (existingSong) {
            if (existingSong != null) {
                res.status(409).json({ error: "La cancion ya esta importada" });
                return;
            }

            const rest = app.get('rest');
            const settings = {
                url: "https://itunes.apple.com/lookup?id=" + encodeURIComponent(trackId) + "&entity=song",
                method: "get",
                json: true
            };

            rest(settings, function (error, response, body) {
                if (error || !response || response.statusCode !== 200 || !body || !body.results || body.results.length === 0) {
                    res.status(404).json({ error: "No se ha encontrado la cancion en iTunes" });
                    return;
                }

                const item = body.results[0];
                const priceEUR = Number.isNaN(requestedPrice) ? Math.round((3 + Math.random() * 7) * 100) / 100 : requestedPrice;

                const importedSong = {
                    trackId: item.trackId,
                    title: item.trackName,
                    artist: item.artistName,
                    author: req.session.user,
                    kind: item.primaryGenreName || '',
                    price: Math.round(priceEUR * 100) / 100,
                    currency: 'EUR',
                    previewUrl: item.previewUrl || '',
                    artworkUrl: item.artworkUrl100 || '',
                    note: '',
                    user: req.session.user,
                    createdAt: new Date(),
                    updatedAt: new Date()
                };

                songsRepository.insertSong(importedSong, function (insertResult) {
                    if (insertResult == null || insertResult.error != null) {
                        res.status(500).json({ error: "Se ha producido un error al importar la cancion" });
                        return;
                    }
                    res.status(201).json({
                        message: "Cancion importada correctamente",
                        _id: insertResult.songId
                    });
                });
            });
        }).catch(function (error) {
            res.status(500).json({ error: "Se ha producido un error al comprobar duplicados " + error });
        });
    });

    app.put('/itunes/:id', function (req, res) {
        if (!ObjectId.isValid(req.params.id)) {
            res.status(400).json({ error: "El ID no es valido" });
            return;
        }

        const songId = new ObjectId(req.params.id);
        const note = (req.body.note || '').trim();

        songsRepository.findSong({ _id: songId }, {}).then(function (song) {
            if (song == null) {
                res.status(404).json({ error: "La cancion importada no existe" });
                return;
            }

            if (song.user !== req.session.user) {
                res.status(403).json({ error: "No tienes permiso para modificar esta cancion" });
                return;
            }

            const updatedSong = {
                note: note,
                updatedAt: new Date()
            };

            songsRepository.updateSong(updatedSong, { _id: songId }, {}).then(function (result) {
                if (result.modifiedCount === 0) {
                    res.status(409).json({ error: "No se ha modificado ninguna cancion" });
                } else {
                    res.status(200).json({
                        message: "Cancion actualizada correctamente",
                        result: result
                    });
                }
            }).catch(function (error) {
                res.status(500).json({ error: "Se ha producido un error al actualizar la cancion " + error });
            });
        }).catch(function (error) {
            res.status(500).json({ error: "Se ha producido un error al buscar la cancion " + error });
        });
    });

    app.delete('/itunes/:id', function (req, res) {
        if (!ObjectId.isValid(req.params.id)) {
            res.status(400).json({ error: "El ID no es valido" });
            return;
        }

        const songId = new ObjectId(req.params.id);
        songsRepository.findSong({ _id: songId }, {}).then(function (song) {
            if (song == null) {
                res.status(404).json({ error: "La cancion importada no existe" });
                return;
            }

            if (song.user !== req.session.user) {
                res.status(403).json({ error: "No tienes permiso para eliminar esta cancion" });
                return;
            }

            songsRepository.deleteSong({ _id: songId }, {}).then(function (result) {
                if (result == null || result.deletedCount === 0) {
                    res.status(404).json({ error: "No se ha podido eliminar la cancion" });
                } else {
                    res.status(200).json({ message: "Cancion eliminada correctamente" });
                }
            }).catch(function (error) {
                res.status(500).json({ error: "Se ha producido un error al eliminar la cancion " + error });
            });
        }).catch(function (error) {
            res.status(500).json({ error: "Se ha producido un error al buscar la cancion " + error });
        });
    });
};
