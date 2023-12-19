import AES from 'crypto-js/aes';
import Crypto from 'crypto-js';

const secretKey = 'YourSecretKey';

const securityService = {

    encryptObject: (object: any) => {
        return AES.encrypt(JSON.stringify(object), secretKey).toString();
    },

    decryptObject: (ciphertext: string) => {
        const bytes = AES.decrypt(ciphertext, secretKey);
        return JSON.parse(bytes.toString(Crypto.enc.Utf8));
    },

}

export {
    securityService
}