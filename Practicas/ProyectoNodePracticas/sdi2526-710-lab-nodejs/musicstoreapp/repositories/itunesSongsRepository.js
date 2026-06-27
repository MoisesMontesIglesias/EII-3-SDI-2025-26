module.exports = {
    dbClient: null,
    app: null,
    database: "musicstoreapp",
    collectionName: "itunes_songs",

    init: function (app, dbClient) {
        this.dbClient = dbClient;
        this.app = app;
    },

    insertItunesSong: async function (song) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const songsCollection = database.collection(this.collectionName);
            const result = await songsCollection.insertOne(song);
            return result.insertedId;
        } catch (error) {
            throw error;
        }
    },

    getItunesSongs: async function (filter, options) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const songsCollection = database.collection(this.collectionName);
            const songs = await songsCollection.find(filter, options).toArray();
            return songs;
        } catch (error) {
            throw error;
        }
    },

    findItunesSong: async function (filter, options) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const songsCollection = database.collection(this.collectionName);
            const song = await songsCollection.findOne(filter, options);
            return song;
        } catch (error) {
            throw error;
        }
    },

    updateItunesSong: async function (newSong, filter, options) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const songsCollection = database.collection(this.collectionName);
            const result = await songsCollection.updateOne(filter, { $set: newSong }, options);
            return result;
        } catch (error) {
            throw error;
        }
    },

    deleteItunesSong: async function (filter, options) {
        try {
            await this.dbClient.connect();
            const database = this.dbClient.db(this.database);
            const songsCollection = database.collection(this.collectionName);
            const result = await songsCollection.deleteOne(filter, options);
            return result;
        } catch (error) {
            throw error;
        }
    }
};
