const { jwtSecretKey } = require('../conf.js');
const jwt = require('jsonwebtoken');

exports.sign = function (user) {
    return jwt.sign(
        {
            id: user.id
        },
        jwtSecretKey,
        { expiresIn: '15s' }
    );
}

exports.verify = function (token) {
    try {
        jwt.verify(token, jwtSecretKey);
        return true;
    } catch (err) {
        return false;
    }
}

exports.getBody = function (token) {
    return JSON.parse(decode64(token.split('.')[1]));
}

function decode64(data) {
    let buff = new Buffer(data, 'base64');
    return buff.toString('ascii');
}
