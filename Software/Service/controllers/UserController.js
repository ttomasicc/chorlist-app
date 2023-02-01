const UserRepository = require('../repositories/UserRepository.js');
const jwt = require('../tools/jwt.js');
const codes = require('../tools/codes.js');

module.exports = class UserController {
    #userRepository;

    constructor() {
        this.#userRepository = new UserRepository();
    }

    async register(req, res) {
        res.type('application/json');

        const user = Object.fromEntries(
            Object.entries({
                firstname: req.body.firstname ? req.body.firstname : '',
                lastname: req.body.lastname ? req.body.lastname : '',
                username: req.body.username ? req.body.username : '',
                email: req.body.email ? req.body.email : '',
                password: req.body.password ? req.body.password : '',
            }).filter(([_, v]) => v !== '')
        );

        if (user.firstname === undefined ||
            user.lastname === undefined ||
            user.username === undefined ||
            user.email === undefined ||
            user.password === undefined) {
            return res.status(422).json({ error: 'missing required attributes' })
        }

        if (await this.#userRepository.findByUsername(user.username) !== null)
            return res.status(409).json({ error: 'user with given username already exists' });
        if (await this.#userRepository.findByEmail(user.email) !== null)
            return res.status(409).json({ error: 'user with given email already exists' });

        await this.#userRepository.add(user);

        return res.status(201).send();
    }

    async login(req, res) {
        res.type('application/json');

        const username = req.body.username.toLowerCase();
        const password = req.body.password;

        const user = await this.#userRepository.findByUsername(username);

        if (user !== null) {
            if (user.password !== codes.createSHA256(password, username + 112)) {
                res.status(401).json({ error: 'wrong password' });
                return;
            }

            req.session.jwt = jwt.sign({ id: user.id });
            res.send();
        } else {
            res.status(404).json({ error: 'user not found' });
        }
    }

    async getJWT(req, res) {
        res.type('application/json');

        if (req.session.jwt != null) {
            const body = jwt.getBody(req.session.jwt);
            res.send({ token: jwt.sign({ id: body.id }) });
        } else {
            res.send({ token: null });
        }
    }

    async logout(req, res) {
        res.type('application/json');
        req.session.jwt = null;
        res.status(204).send();
    }

    async update(req, res) {
        res.type('application/json');

        const user = Object.fromEntries(
            Object.entries({
                id: jwt.getBody(req.session.jwt).id,
                firstname: req.body.firstname ? req.body.firstname : '',
                lastname: req.body.lastname ? req.body.lastname : '',
                password: req.body.password ? req.body.password : '',
            }).filter(([_, v]) => v !== '')
        );

        if (user.firstname === undefined &&
            user.lastname === undefined &&
            user.password === undefined) {
            return res.status(422).json({ error: 'missing required attributes' })
        }

        const fetchedUser = await this.#userRepository.findById(user.id);

        if (fetchedUser !== null) {
            fetchedUser.firstname = user.firstname === undefined ? fetchedUser.firstname : user.firstname;
            fetchedUser.lastname = user.lastname === undefined ? fetchedUser.lastname : user.lastname;
            fetchedUser.password = user.lastname === undefined ? codes.createSHA256(user.password, fetchedUser.username + 112) : user.password;

            await this.#userRepository.update(fetchedUser);
            return res.status(200).send();
        } else {
            return res.status(404).json({ error: 'user not found' });
        }
    }

    async get(req, res){
        res.type('application/json');

        return res.status(200).json(await this.#userRepository.findById(jwt.getBody(req.session.jwt).id)) ;
    }
}