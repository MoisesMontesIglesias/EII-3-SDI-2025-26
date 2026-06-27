const { ObjectId } = require("mongodb");

module.exports = function (app, songsRepository) {
    function getCurrentUser(req, res) {
        return req.user || res.user || null;
    }

    function mapSongForClient(song) {
        return Object.assign({}, song, {
            id: song._id.toString()
        });
    }

    app.get('/api/v1.0/itunes/search', function (req, res) {
        const term = (req.query.term || '').trim();

        if (term.length === 0) {
            res.status(400).json({ error: "El termino de busqueda es obligatorio" });
            return;
        }

        const rest = app.get('rest');
        const settings = {
            url: "https://itunes.apple.com/search?term=" +
                encodeURIComponent(term) +
                "&country=US&media=music&entity=song&limit=12",
            method: "get",
            json: true
        };

        rest(settings, function (error, response, body) {
            if (error || !response || response.statusCode !== 200) {
                res.status(500).json({ error: "Se ha producido un error al consultar iTunes" });
                return;
            }

            const results = body && body.results ? body.results.map(function (item) {
                return {
                    trackId: item.trackId,
                    trackName: item.trackName || '',
                    artistName: item.artistName || '',
                    primaryGenreName: item.primaryGenreName || '',
                    trackPrice: item.trackPrice,
                    currency: item.currency || 'USD',
                    artworkUrl100: item.artworkUrl100 || '',
                    previewUrl: item.previewUrl || ''
                };
            }) : [];

            res.status(200).json({
                term: term,
                results: results
            });
        });
    });

    app.get('/api/v1.0/itunes/library', async function (req, res) {
        const user = getCurrentUser(req, res);

        if (!user) {
            res.status(403).json({ error: "No hay Token" });
            return;
        }

        try {
            const songs = await songsRepository.getSongs({
                user: user,
                trackId: { $exists: true }
            }, { sort: { createdAt: -1 } });

            res.status(200).json({
                songs: songs.map(mapSongForClient)
            });
        } catch (error) {
            res.status(500).json({ error: "Se ha producido un error al listar la biblioteca " + error.message });
        }
    });

    app.post('/api/v1.0/itunes/import', function (req, res) {
        const user = getCurrentUser(req, res);
        const trackId = req.body.trackId;
        const requestedPrice = parseFloat(req.body.priceEur);

        if (!user) {
            res.status(403).json({ error: "No hay Token" });
            return;
        }

        if (trackId == null || trackId === '') {
            res.status(400).json({ error: "Falta el identificador de iTunes" });
            return;
        }

        const duplicateFilter = {
            user: user,
            trackId: parseInt(trackId, 10)
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
                const priceEUR = Number.isNaN(requestedPrice) ? (typeof item.trackPrice === 'number' ? item.trackPrice : 0) : requestedPrice;

                const importedSong = {
                    trackId: item.trackId,
                    title: item.trackName,
                    artist: item.artistName,
                    author: user,
                    kind: item.primaryGenreName || '',
                    price: Math.round(priceEUR * 100) / 100,
                    currency: item.currency || 'EUR',
                    previewUrl: item.previewUrl || '',
                    artworkUrl: item.artworkUrl100 || '',
                    note: '',
                    user: user,
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
            res.status(500).json({ error: "Se ha producido un error al comprobar duplicados " + error.message });
        });
    });

    app.put('/api/v1.0/itunes/:id', async function (req, res) {
        const user = getCurrentUser(req, res);

        if (!user) {
            res.status(403).json({ error: "No hay Token" });
            return;
        }

        if (!ObjectId.isValid(req.params.id)) {
            res.status(400).json({ error: "El ID no es valido" });
            return;
        }

        const songId = new ObjectId(req.params.id);
        const note = (req.body.note || '').trim();

        try {
            const song = await songsRepository.findSong({ _id: songId }, {});

            if (song == null) {
                res.status(404).json({ error: "La cancion importada no existe" });
                return;
            }

            if (song.user !== user) {
                res.status(403).json({ error: "No tienes permiso para modificar esta cancion" });
                return;
            }

            const updatedSong = {
                note: note,
                updatedAt: new Date()
            };

            const result = await songsRepository.updateSong(updatedSong, { _id: songId }, {});

            if (result.modifiedCount === 0) {
                res.status(409).json({ error: "No se ha modificado ninguna cancion" });
            } else {
                res.status(200).json({
                    message: "Cancion actualizada correctamente",
                    result: result
                });
            }
        } catch (error) {
            res.status(500).json({ error: "Se ha producido un error al actualizar la cancion " + error.message });
        }
    });

    app.delete('/api/v1.0/itunes/:id', async function (req, res) {
        const user = getCurrentUser(req, res);

        if (!user) {
            res.status(403).json({ error: "No hay Token" });
            return;
        }

        if (!ObjectId.isValid(req.params.id)) {
            res.status(400).json({ error: "El ID no es valido" });
            return;
        }

        const songId = new ObjectId(req.params.id);

        try {
            const song = await songsRepository.findSong({ _id: songId }, {});

            if (song == null) {
                res.status(404).json({ error: "La cancion importada no existe" });
                return;
            }

            if (song.user !== user) {
                res.status(403).json({ error: "No tienes permiso para eliminar esta cancion" });
                return;
            }

            const result = await songsRepository.deleteSong({ _id: songId }, {});

            if (result == null || result.deletedCount === 0) {
                res.status(404).json({ error: "No se ha podido eliminar la cancion" });
            } else {
                res.status(200).json({ message: "Cancion eliminada correctamente" });
            }
        } catch (error) {
            res.status(500).json({ error: "Se ha producido un error al eliminar la cancion " + error.message });
        }
    });
};
