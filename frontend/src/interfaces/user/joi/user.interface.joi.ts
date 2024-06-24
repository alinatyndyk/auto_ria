import Joi from 'joi';

const userResponseSchema = Joi.object({ //todo fix object
    id: Joi.number().allow(null),
    name: Joi.string().allow(null),
    lastName: Joi.string().allow(null),
    city: Joi.string().allow(null),
    region: Joi.string().allow(null),
    number: Joi.string().allow(null),
    avatar: Joi.string().optional().allow(null), // avatar может быть null
    accountType: Joi.string().valid('BASIC', 'PREMIUM').required(), // предполагая, что у вас есть два типа аккаунтов: BASIC и PREMIUM
    role: Joi.string().valid('USER', 'ADMIN', 'MANAGER').required(), // предполагая, что роли могут быть USER, ADMIN, MANAGER
    isPaymentSourcePresent: Joi.boolean().allow(null),
    paymentSourcePresent: Joi.boolean().allow(null),  // Allow paymentSourcePresent
    lastOnline: Joi.array().items(Joi.number()).required().allow(null), // lastOnline может быть null
    // createdAt: Joi.date().iso().required()
    createdAt: Joi.array().items(Joi.number()).required()
});

export const validateUserSQL = (user: any) => {
    const { error } = userResponseSchema.validate(user);
    if (error) {
        console.error('Validation error:', error.details);
        return false;
    }
    return true;
}