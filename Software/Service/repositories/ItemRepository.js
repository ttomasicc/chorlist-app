const DB = require('../DB.js');

module.exports = class ItemRepository {
    #db;

    constructor() {
        this.#db = new DB();
    }

    async search(query, userId) {
        const sql = `SELECT i.id, i.id_shopping_list, i.description FROM item i
            JOIN shopping_list sl ON i.id_shopping_list = sl.id
            WHERE sl.id_user = $1 and i.description ILIKE $2;`;

        const results = await this.#db.query(sql, [userId, `%${query}%`]);

        if (results.length !== 0)
            return results;
        return null;
    }

    async getItemById(id, userId) {
        const sql = `SELECT i.id, i.id_shopping_list, i.description FROM item i
            JOIN shopping_list sl ON i.id_shopping_list = sl.id
            WHERE i.id = $1 AND sl.id_user = $2;`;

        const results = await this.#db.query(sql, [id, userId]);
      
        if (results.length !== 0)
            return results[0];
        return null;
    }

    async getForShoppingList(id) {
        const sql = 'SELECT * FROM item WHERE id_shopping_list = $1;';

        const results = await this.#db.query(sql, [id]);
      
        if (results.length !== 0)
            return results;
        return null;
    }

    async add(item) {
        const sql = 'INSERT INTO item (id_shopping_list, description) VALUES ($1, $2) RETURNING *;';
        const data = [item.id_shopping_list, item.description];

        const results = await this.#db.query(sql, data);

        return results[0];
    }

    async update(item, id) {
        const sql = 'UPDATE item SET description = COALESCE($1, description) WHERE id = $2;';
        const data = [item.description, id];

        await this.#db.query(sql, data);

        return true;
    }

    async deleteById(id) {
        const sql = 'DELETE FROM item WHERE id = $1';

        await this.#db.query(sql, [id]);

        return true;
    }
}