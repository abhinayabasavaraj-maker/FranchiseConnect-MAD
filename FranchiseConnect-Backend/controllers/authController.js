const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const User = require('../models/userModel');

const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET || 'your_super_secret_key_here', {
        expiresIn: '30d',
    });
};

exports.register = async (req, res) => {
    try {
        const { email, password } = req.body;
        const existingUser = await User.findOne({ email });
        if (existingUser) return res.status(400).json({ message: "User already exists" });

        const hashedPassword = await bcrypt.hash(password, 10);
        const newUser = new User({ ...req.body, password: hashedPassword });
        await newUser.save();
        res.status(201).json({ message: "User registered successfully" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.login = async (req, res) => {
    try {
        const { email, password } = req.body;
        const user = await User.findOne({ email });
        if (!user) return res.status(400).json({ message: "User not found" });

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) return res.status(400).json({ message: "Invalid credentials" });

        res.json({
            message: "Login successful",
            token: generateToken(user._id),
            user: { id: user._id, email: user.email, name: user.firstName, mobile: user.mobile }
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.verifyDetails = async (req, res) => {
    try {
        const { email, dob, state, city, pincode, gender } = req.body;
        const user = await User.findOne({ email });
        if (!user) return res.status(404).json({ message: "User not found" });

        // Normalize state and gender comparisons (case insensitive / trim whitespace)
        const normalize = (val) => (val || "").toString().trim().toLowerCase();
        
        if (normalize(user.dob) !== normalize(dob) ||
            normalize(user.state) !== normalize(state) ||
            normalize(user.city) !== normalize(city) ||
            normalize(user.pincode) !== normalize(pincode) ||
            normalize(user.gender) !== normalize(gender)) {
            return res.status(400).json({ message: "Security details do not match our records." });
        }

        res.json({ 
            message: "Details verified successfully",
            user: { id: user._id, email: user.email, name: user.firstName }
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.resetPassword = async (req, res) => {
    try {
        const { userId, email, newPassword } = req.body;
        const hashedPassword = await bcrypt.hash(newPassword, 10);
        
        if (userId) {
            await User.findByIdAndUpdate(userId, { password: hashedPassword });
        } else if (email) {
            await User.findOneAndUpdate({ email }, { password: hashedPassword });
        } else {
            return res.status(400).json({ message: "Identifier (email or userId) is required." });
        }
        
        res.json({ message: "Password reset successful" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
