const ItemRepository = require('../repositories/ItemRepository.js');
const ShoppingListRepository = require('../repositories/ShoppingListRepository.js');
const jwt = require("../tools/jwt.js");

module.exports = class ShoppingListController {
    #shoppingListRepository;
    #itemRepository;

    constructor() {
        this.#shoppingListRepository = new ShoppingListRepository();
        this.#itemRepository = new ItemRepository();
    }

    async getAll(req, res) {
        res.type('application/json');

        const userId = jwt.getBody(req.session.jwt).id;
        const shoppingLists = await this.#shoppingListRepository.getAll({ id_user: userId });

        if (shoppingLists !== null)
            return res.status(200).json(shoppingLists);
        res.status(204).send();
    }

    async get(req, res) {
        res.type('application/json');

        const shoppingList = await this.#shoppingListRepository.get({
            id: req.params.id,
            id_user: jwt.getBody(req.session.jwt).id
        });

        if (shoppingList !== null) {
            const items = await this.#itemRepository.getForShoppingList(shoppingList.id);
            shoppingList.items = items ? items : [];
            return res.status(200).json(shoppingList);
        }

        res.status(404).json({ error: 'shopping list not found' });
    }

    async delete(req, res) {
        res.type('application/json');

        const shoppingList = await this.#shoppingListRepository.get({
            id: req.params.id,
            id_user: jwt.getBody(req.session.jwt).id
        });

        if (shoppingList !== null) {
            await this.#shoppingListRepository.delete(shoppingList);
            return res.status(200).send();
        }

        res.status(404).json({ error: 'shopping list not found' });
    }

    async update(req, res) {
        res.type('application/json');

        const shoppingList = {
            id: req.params.id,
            id_user: jwt.getBody(req.session.jwt).id,
            description: req.body.description,
            color: req.body.color
        };

        if (shoppingList.color)
            if (!/^#[A-Fa-f0-9]{6}$/.test(shoppingList.color))
                return res.status(422).json({ error: 'invalid color' });

        if (await this.#shoppingListRepository.get(shoppingList) === null)
            return res.status(404).json({ error: 'shopping list not found' });

        await this.#shoppingListRepository.update(shoppingList);

        res.status(200).send();
    }

    async add(req, res) {
        res.type('application/json');

        const shoppingListReq = Object.fromEntries(
            Object.entries({
                description: req.body.description ? req.body.description : '',
                color: req.body.color ? req.body.color : '',
            }).filter(([_, v]) => v !== '')
        );

        if (shoppingListReq.description === undefined || shoppingListReq.color === undefined) {
            return res.status(422).json({ error: 'missing required attributes' });
        }
        if (!/^#[A-Fa-f0-9]{6}$/.test(shoppingListReq.color)) {
            return res.status(422).json({ error: 'invalid color' });
        }

        shoppingListReq.id_user = jwt.getBody(req.session.jwt).id;
        const shoppingList = await this.#shoppingListRepository.save(shoppingListReq);

        res.status(201).json(shoppingList);
    }
}