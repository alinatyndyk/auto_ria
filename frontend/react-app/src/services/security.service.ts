import AES from 'crypto-js/aes';
import Crypto from 'crypto-js';

const secretKey = 'YourSecretKey';

const securityService = {

    // Encrypt the object
    encryptObject: (object: any) => {
        return AES.encrypt(JSON.stringify(object), secretKey).toString();
    },

// Decrypt the encrypted object
    decryptObject: (ciphertext: string) => {
        const bytes = AES.decrypt(ciphertext, secretKey);
        return JSON.parse(bytes.toString(Crypto.enc.Utf8));
    },

}

export {
    securityService
}