const DB = require('../DB.js');

module.exports = class ShoppingListRepository {
    #db;

    constructor() {
        this.#db = new DB();
    }

    async getAll(shoppingList) {
        const sql = 'SELECT * FROM shopping_list WHERE id_user = $1;';

        const results = await this.#db.query(sql, [shoppingList.id_user]);

        if (results.length !== 0)
            return results;
        return null;
    }

    async get(shoppingList) {
        const data = [shoppingList.id, shoppingList.id_user];
        const sql = 'SELECT * FROM shopping_list WHERE id = $1 AND id_user = $2;';

        const results = await this.#db.query(sql, data);

        if (results.length !== 0)
            return results[0];
        return null;
    }

    async delete(shoppingList) {
        const sql_child = 'DELETE FROM item WHERE id_shopping_list = $1;';
        const sql = 'DELETE FROM shopping_list WHERE id = $1 AND id_user = $2;';

        await this.#db.query(sql_child, [shoppingList.id]);
        await this.#db.query(sql, [shoppingList.id, shoppingList.id_user]);

        return true;
    }

    async update(shoppingList) {
        const data = [
            shoppingList.description,
            shoppingList.color,
            shoppingList.id,
            shoppingList.id_user
        ];
        const sql = `
            UPDATE shopping_list
                SET
                    description = COALESCE($1, description),
                    color = COALESCE($2, color),
                    modified = CURRENT_DATE
                WHERE 
                    id = $3
                AND
                    id_user = $4;`;

        await this.#db.query(sql, data);

        return true;
    }

    async save(shoppingList) {
        const data = [
            shoppingList.id_user,
            shoppingList.description,
            shoppingList.color
        ];
        const sql = `INSERT INTO shopping_list (id_user, description, color, modified)
                VALUES($1, $2, $3, CURRENT_DATE) RETURNING *;`;

        const results = await this.#db.query(sql, data);

        return results[0];
    }
}