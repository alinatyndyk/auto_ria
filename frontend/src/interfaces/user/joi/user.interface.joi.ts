import Joi from 'joi';

const userResponseSchema = Joi.object({
    id: Joi.number(),
    name: Joi.string(),
    lastName: Joi.string(),
    city: Joi.string(),
    region: Joi.string(),
    number: Joi.string(),
    avatar: Joi.string().allow(null),
    accountType: Joi.string().valid('BASIC', 'PREMIUM').required(),
    role: Joi.string().valid('USER', 'ADMIN', 'MANAGER').required(),
    isPaymentSourcePresent: Joi.boolean().allow(null),
    paymentSourcePresent: Joi.boolean().allow(null), 
    lastOnline: Joi.array().items(Joi.number()).required().allow(null),
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