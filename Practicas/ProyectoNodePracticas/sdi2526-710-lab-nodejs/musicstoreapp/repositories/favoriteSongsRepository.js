module.exports = {
    dbClient: null,
    app: null,
    database: "musicStore",
    collectionName: "favorite_songs",

    init: function (app, dbClient) {
        this.dbClient = dbClient;
        this.app = app;
    },

    addFavorite: async function (favorite) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const favoritesCollection = database.collection(this.collectionName);
            const result = await favoritesCollection.insertOne(favorite);
            return result.insertedId;
        } catch (error) {
            throw error;
        }
    },

    getFavorites: async function (filter, options) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const favoritesCollection = database.collection(this.collectionName);
            const favorites = await favoritesCollection.find(filter, options).toArray();
            return favorites;
        } catch (error) {
            throw error;
        }
    },
    deleteFavorite: async function (filter, options) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const favoritesCollection = database.collection(this.collectionName);
            const result = await favoritesCollection.deleteMany(filter, options);
            return result;
        } catch (error) {
            throw error;
        }
    }
};