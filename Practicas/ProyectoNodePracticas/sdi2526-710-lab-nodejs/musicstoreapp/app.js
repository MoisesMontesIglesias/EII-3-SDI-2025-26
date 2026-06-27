let createError = require('http-errors');
let express = require('express');
let path = require('path');
let cookieParser = require('cookie-parser');
let logger = require('morgan');
let  swaggerUi = require('swagger-ui-express');
let  swaggerJsdoc = require('swagger-jsdoc');
let { Song } = require('./schemas/song.schema');
let { SongRequest } = require('./schemas/songRequest.schema');

let app = express();
let rest = require('request');
app.set('rest', rest);
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Credentials", "true");
  res.header("Access-Control-Allow-Methods", "POST, GET, DELETE, UPDATE, PUT");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type,Accept, token");
// Debemos especificar todas las headers que se aceptan. Content-Type , token
  next();
});
app.options("*", function(req, res) {
  res.sendStatus(204);
});
let jwt = require('jsonwebtoken');
app.set('jwt', jwt);
let expressSession = require('express-session');
app.use(expressSession({
  secret: 'abcdefg',
  resave: true,
  saveUninitialized: true
}));

let crypto = require('crypto');
let fileUpload = require('express-fileupload');
app.use(fileUpload({
  limits: { fileSize: 50 * 1024 * 1024 },
  createParentPath: true
}));
app.set('uploadPath', __dirname)
app.set('clave','abcdefg');
app.set('crypto',crypto);
let bodyParser = require('body-parser');
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(function (req, res, next) {
  res.redirectError = function (message, statusCode, messageType) {
    const status = statusCode || 500;
    const type = messageType || (status >= 500 ? 'alert-danger' : 'alert-warning');
    res.redirect('/error?status=' + encodeURIComponent(status) +
        '&message=' + encodeURIComponent(message) +
        '&messageType=' + encodeURIComponent(type));
  };
  next();
});

const { MongoClient } = require("mongodb");
const connectionStrings = 'mongodb://admin:sdi2526-710@ac-c9mfc5u-shard-00-00.qxxet7j.mongodb.net:27017,ac-c9mfc5u-shard-00-01.qxxet7j.mongodb.net:27017,ac-c9mfc5u-shard-00-02.qxxet7j.mongodb.net:27017/?ssl=true&replicaSet=atlas-4fvq5b-shard-0&authSource=admin&appName=musicstoreapp'
const dbClient = new MongoClient(connectionStrings);

const usersRepository = require("./repositories/usersRepository.js");
usersRepository.init(app, dbClient);
require("./routes/users.js")(app, usersRepository);

let indexRouter = require('./routes/index');
const userSessionRouter = require('./routes/userSessionRouter');
const userAudiosRouter = require('./routes/userAudiosRouter');
app.use("/songs/add",userSessionRouter);
app.use("/publications",userSessionRouter);
app.use("/songs/buy",userSessionRouter);
app.use("/purchases",userSessionRouter);
app.use("/audios/",userAudiosRouter);
app.use("/shop/",userSessionRouter)
app.use("/itunes", userSessionRouter);

const userAuthorRouter = require('./routes/userAuthorRouter');
app.use("/songs/edit",userAuthorRouter);
app.use("/songs/delete",userAuthorRouter);
const userTokenRouter = require('./routes/userTokenRouter');
app.use("/api/v1.0/songs/", userTokenRouter);
app.use("/api/v1.0/itunes/", userTokenRouter);
app.use("/songs/favorites",userSessionRouter)
let songsRepository = require("./repositories/songsRepository.js");
songsRepository.init(app, dbClient);
require("./routes/songs.js")(app, songsRepository);
let itunesSongsRepository = require("./repositories/itunesSongsRepository.js");
itunesSongsRepository.init(app, dbClient);
require("./routes/itunes.js")(app, songsRepository);
require("./routes/api/songsAPIv1.0.js")(app, songsRepository, usersRepository);
require("./routes/api/itunesAPIv1.0.js")(app, songsRepository);
require("./routes/authors.js")(app);
let favoriteSongsRepository = require("./repositories/favoriteSongsRepository.js");
favoriteSongsRepository.init(app, dbClient);
require("./routes/favorites.js")(app, favoriteSongsRepository, songsRepository);

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'twig');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', indexRouter);

app.get('/error', function (req, res) {
  const parsedStatus = parseInt(req.query.status || '500', 10);
  const status = Number.isInteger(parsedStatus) ? parsedStatus : 500;
  res.status(status);
  res.render('error', {
    status: status,
    message: req.query.message || 'Se ha producido un error',
    messageType: req.query.messageType || 'alert-danger'
  });
});

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'API de la tienda de musica de SDI',
      version: '1.0.0',
      description: 'Documentacion interactiva de la API',
    },
    components: {
      schemas: {
        Song,
        SongRequest
      },
    },
    servers: [
      {
        url: 'http://localhost:8081',
        description: 'Servidor de pruebas de la aplicacion',
      },
    ],
  },
  apis: [path.resolve(__dirname, 'routes/api/*.js').replace(/\\/g, '/')],
};

const swaggerDocs = swaggerJsdoc(swaggerOptions);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocs));

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  console.log("Se ha producido un error " + err);
  const status = err.status || err.statusCode || 500;
  const messageType = status >= 500 ? 'alert-danger' : 'alert-warning';
  res.redirectError(err.message || 'Se ha producido un error', status, messageType);
});

module.exports = app;
