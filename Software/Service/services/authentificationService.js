const jwt = require('../tools/jwt.js');

exports.authentificate = function (req, res, next) {
    if (req.headers.authorization != null)
        if (jwt.verify(req.headers.authorization.split(' ')[1]))
            return next();
    return res.status(401).send();
}
