const crypto = require('crypto');

exports.createSHA256 = function (text, salt) {
    const hash = crypto.createHash('sha256');
    hash.write(text + salt);
    let output = hash.digest('hex');
    hash.end();
    return output;
}
