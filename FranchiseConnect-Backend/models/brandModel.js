const mongoose = require('mongoose');

const BrandSchema = new mongoose.Schema({
    name: String,
    category: String,
    investment: String,
    logoUrl: String,
    ownerEmail: String,
    ownerMobile: String
});

module.exports = mongoose.model('Brand', BrandSchema);
