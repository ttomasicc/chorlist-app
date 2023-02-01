const ItemRepository = require('../repositories/ItemRepository');
const ShoppingListRepository = require("../repositories/ShoppingListRepository");
const jwt = require("../tools/jwt");

module.exports = class ItemController {
    #itemRepository;
    #shoppingListRepository;

    constructor() {
        this.#itemRepository = new ItemRepository();
        this.#shoppingListRepository = new ShoppingListRepository();
    }

    async search(req, res) {
        res.type('application/json');
        const query = req.query.query;
        const userId = jwt.getBody(req.session.jwt).id;

        if (query === '') {
            res.status(404).json({ error: 'invalid query parameter' });
        } else {
            const items = await this.#itemRepository.search(query, userId);
            if (items !== null) {
                res.status(200).json(items);
            } else {
                res.status(204).send();
            }
        }
    }

    async add(req, res) {
        res.type('application/json');
        const itemReq = req.body;
        const userId = jwt.getBody(req.session.jwt).id;

        if (itemReq.id_shopping_list === undefined || itemReq.description === undefined) {
            res.status(404).json({ error: 'missing required attributes' });
        } else if (!(await this.#isAuthorizedShoppingList(userId, itemReq.id_shopping_list))) {
            res.status(403).json({ error: 'invalid shopping list id' });
        } else if (itemReq.description.trim() === '') {
            res.status(404).json({ error: 'cannot insert empty values' });
        } else {
            try {
                const item = await this.#itemRepository.add(itemReq);
                res.status(201).json(item);
            } catch (error) {
                res.status(404).json({ error: 'item cannot be added' });
            }
        }
    }

    async update(req, res) {
        res.type('application/json');
        const item = req.body;
        const id = req.params.id;
        const userId = jwt.getBody(req.session.jwt).id;

        if (!(await this.#isAuthorizedItem(userId, id))) {
            res.status(403).json({ error: 'invalid item id' })
        } else if (item.description.trim() === '') {
            res.status(404).json({ error: 'cannot update with empty value' });
        } else {
            await this.#itemRepository.update(item, id);
            res.status(200).send();
        }
    }

    async delete(req, res) {
        res.type('application/json');
        const id = req.params.id;
        const userId = jwt.getBody(req.session.jwt).id;

        if (!(await this.#isAuthorizedItem(userId, id))) {
            res.status(403).json({ error: 'invalid item id' })
        } else {
            await this.#itemRepository.deleteById(id);
            res.status(200).send();
        }
    }

    async #isAuthorizedShoppingList(userId, shoppingListId) {
        const shoppingList = await this.#shoppingListRepository.get({ id: shoppingListId, id_user: userId });
        return shoppingList !== null;
    }

    async #isAuthorizedItem(userId, itemId) {
        const item = await this.#itemRepository.getItemById(itemId, userId);
        return item !== null;
    }
}