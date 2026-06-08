const Brand = require('../models/brandModel');

exports.getBrands = async (req, res) => {
    try {
        const brands = await Brand.find();
        res.json(brands);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.createBrand = async (req, res) => {
    try {
        const newBrand = new Brand(req.body);
        await newBrand.save();
        res.status(201).json(newBrand);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.seedBrands = async (req, res) => {
    try {
        await Brand.deleteMany({});
        const sampleBrands = [
            { name: "Burger King", category: "Food & Beverages", investment: "₹1.5Cr - 3Cr", logoUrl: "https://logo.clearbit.com/burgerking.com", ownerEmail: "abbaskhan.cs25@bmsce.ac.in", ownerMobile: "9876543210" },
            { name: "Lakme Salon", category: "Beauty & Salon", investment: "₹50L - 1Cr", logoUrl: "https://logo.clearbit.com/lakme.com", ownerEmail: "abhinaya.cs24@bmsce.ac.in", ownerMobile: "9876543211" },
            { name: "Tealogy", category: "Food & Beverages", investment: "₹10L - 20L", logoUrl: "https://logo.clearbit.com/tealogy.in", ownerEmail: "abhishek.cs25@bmsce.ac.in", ownerMobile: "9876543212" }
        ];
        await Brand.insertMany(sampleBrands);
        res.send("✅ Sample data added successfully!");
    } catch (err) {
        res.status(500).send(err.message);
    }
};
