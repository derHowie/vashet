var express = require('express');
var http = require('http');
var serveStatic = require('serve-static');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var session = require('express-session');
var cors = require('cors');

var port = 9000;

var app = express();

app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

app.use(serveStatic('resources/public', {
    index: ['index.html']
}));

http.createServer(app).listen(port, function () {
    'use strict';
    console.log('Server running on port ' + port);
});
