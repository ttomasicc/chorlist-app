const DB = require('../DB.js');
const codes = require('../tools/codes.js');

module.exports = class UserRepository {
    #db;

    constructor() {
        this.#db = new DB();
    }

    async add(user) {
        user.username = user.username.toLowerCase();

        const sql = 'INSERT INTO user_account (firstname, lastname, username, email, "password")' +
            'VALUES ($1, $2, $3, $4, $5)';
        const data = [
            user.firstname,
            user.lastname,
            user.username,
            user.email,
            codes.createSHA256(user.password, user.username + 112),
        ];

        await this.#db.query(sql, data);

        return true;
    }

    async findById(id) {
        const sql = 'SELECT * FROM user_account WHERE id = $1';

        const results = await this.#db.query(sql, [id]);

        if (results.length !== 0)
            return results[0];
        return null;
    }

    async findByUsername(username) {
        const uname = username.toLowerCase();
        const sql = 'SELECT * FROM user_account WHERE username = $1';

        const results = await this.#db.query(sql, [uname]);

        if (results.length !== 0)
            return results[0];
        return null;
    }

    async findByEmail(email) {
        const sql = 'SELECT * FROM user_account WHERE email = $1';

        const results = await this.#db.query(sql, [email]);

        if (results.length !== 0)
            return results[0];
        return null;
    }

    async update(user) {
        const sql = 'UPDATE user_account SET firstname = $1, lastname = $2, password = $3 WHERE id = $4';

        await this.#db.query(sql, [user.firstname, user.lastname, user.password, user.id]);

        return true;
    }
}
