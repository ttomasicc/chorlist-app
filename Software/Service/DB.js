const conf = require('./conf.js');
const pg = require('pg');

module.exports = class DB {
    #pool;
    #client;

    constructor() {
        this.#pool = new pg.Pool({
            user: conf.dbUser,
            host: conf.dbServer,
            database: conf.dbName,
            password: conf.dbPassword,
            port: conf.dbPort,
            max: 20
        });
    }

    async query(sql, data) {
        const client = await this.#pool.connect();

        try {
            await client.query('BEGIN');
            try {
                const { rows } = await client.query(sql, data);
                await client.query('COMMIT');
                return rows;
            } catch (err) {
                await client.query('ROLLBACK');
                throw err;
            }
        } finally {
            client.release();
        }
    }
}