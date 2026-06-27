const ObjectId = require('mongodb').ObjectId;

module.exports = function (app, favoriteSongsRepository, songsRepository) {
    app.get('/songs/favorites', function (req, res) {
        let filter = { user: req.session.user };
        let options = { sort: { date: -1 } };
        favoriteSongsRepository.getFavorites(filter, options).then(function (favorites) {
            let total = favorites.reduce(function (sum, favorite) {
                return sum + parseFloat(favorite.price);
            }, 0);
            res.render('songs/favorites.twig', {
                favorites: favorites,
                total: total
            });
        }).catch(function (error) {
            res.redirectError('Se ha producido un error al listar los favoritos ' + error, 500);
        });
    });

    function addFavoriteHandler(req, res) {
        let songId = req.params.song_id;
        if (!ObjectId.isValid(songId)) {
            res.redirectError('El ID de la cancion no es valido', 400);
            return;
        }

        let filter = { _id: new ObjectId(songId) };
        songsRepository.findSong(filter, {}).then(function (song) {
            if (song == null) {
                res.redirectError('La cancion no existe', 404);
            } else {
                let favorite = {
                    song_id: new ObjectId(songId),
                    user: req.session.user,
                    date: new Date(),
                    price: parseFloat(song.price),
                    title: song.title
                };
                favoriteSongsRepository.deleteFavorite(
                    { user: req.session.user, title: song.title },
                    {}
                ).then(function () {
                    return favoriteSongsRepository.addFavorite(favorite);
                }).then(function () {
                    res.redirect('/songs/favorites');
                }).catch(function (error) {
                    res.redirectError('Se ha producido un error al anadir la cancion a favoritos ' + error, 500);
                });
            }
        }).catch(function (error) {
            res.redirectError('Se ha producido un error al buscar la cancion ' + error, 500);
        });
    }

    app.post('/songs/favorites/add/:song_id', addFavoriteHandler);
    app.get('/songs/favorites/add/:song_id', addFavoriteHandler);

    app.get('/songs/favorites/delete/:song_id', function (req, res) {
        let songId = req.params.song_id;
        let filter = {
            user: req.session.user,
            $or: [
                { song_id: new ObjectId(songId) },
                { song_id: songId }
            ]
        };
        let options = {};
        favoriteSongsRepository.deleteFavorite(filter, options).then(function (result) {
            if (result && result.deletedCount === 1) {
                res.redirect('/songs/favorites');
            } else {
                res.redirectError('No se ha podido eliminar la cancion de favoritos', 404);
            }
        }).catch(function (error) {
            res.redirectError('Se ha producido un error al eliminar la cancion de favoritos ' + error, 500);
        });
    });
};
