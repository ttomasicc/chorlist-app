const { sessionSecretKey } = require('./conf.js');
const { query } = require('express-validator');
const express = require('express');
const cookies = require('cookie-parser');
const session = require('express-session');

const UserController = require('./controllers/UserController.js');
const ShoppingListController = require('./controllers/ShoppingListController.js');
const ItemController = require('./controllers/ItemController.js');

const authService = require('./services/authentificationService.js');
const port = process.env.PORT | 3000;
const app = express();

app.use(express.urlencoded({ extended: true }));
app.use(express.json());
app.use(cookies());
app.use(session({
    secret: sessionSecretKey,
    saveUninitialized: true,
    cookie: { maxAge: 1000 * 60 * 60 * 3 },
    resave: false
}));

setUpResources();

app.use((req, res) => res.status(404).send());

app.listen(port, () =>
    console.info(`\u2713 Application started on port: ${port}`)
);

function setUpResources() {
    setUpUserResources();
    setUpShoppingListResources();
    setUpItemResources();
}

function setUpUserResources() {
    const userController = new UserController();

    app.post('/api/users/register', userController.register.bind(userController));

    app.post('/api/users/login', userController.login.bind(userController));
    app.get('/api/users/logout', userController.logout.bind(userController));

    app.get('/api/users/jwt', userController.getJWT.bind(userController));

    app.put('/api/users/update', [authService.authentificate], userController.update.bind(userController));
    app.get('/api/users/current', [authService.authentificate], userController.get.bind(userController));
}

function setUpShoppingListResources() {
    const shoppingListController = new ShoppingListController();

    app.get('/api/shoppinglists', [authService.authentificate], shoppingListController.getAll.bind(shoppingListController));
    app.post('/api/shoppinglists', [authService.authentificate], shoppingListController.add.bind(shoppingListController));
    app.get('/api/shoppinglists/:id', [authService.authentificate], shoppingListController.get.bind(shoppingListController));
    app.put('/api/shoppinglists/:id', [authService.authentificate], shoppingListController.update.bind(shoppingListController));
    app.delete('/api/shoppinglists/:id', [authService.authentificate], shoppingListController.delete.bind(shoppingListController));
}

function setUpItemResources() {
    const itemController = new ItemController();

    app.get('/api/items', [authService.authentificate, query('query').trim().escape()], itemController.search.bind(itemController));
    app.post('/api/items', [authService.authentificate], itemController.add.bind(itemController));
    app.put('/api/items/:id', [authService.authentificate], itemController.update.bind(itemController));
    app.delete('/api/items/:id', [authService.authentificate], itemController.delete.bind(itemController));
}