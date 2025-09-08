import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

// Define enums to match your backend
const RiskTolerance = {
  CONSERVATIVE: 'CONSERVATIVE',
  MODERATE: 'MODERATE',
  AGGRESSIVE: 'AGGRESSIVE'
} as const;

const SecurityQuestion = {
  MOTHER_MAIDEN_NAME: "MOTHER_MAIDEN_NAME",
  FIRST_PET_NAME: "FIRST_PET_NAME",
  HIGH_SCHOOL_NAME: "HIGH_SCHOOL_NAME"
} as const;

const Currency = {
  USD: "USD"
} as const;

type RiskToleranceType = typeof RiskTolerance[keyof typeof RiskTolerance];
type SecurityQuestionType =
  (typeof SecurityQuestion)[keyof typeof SecurityQuestion];
type CurrencyType = (typeof Currency)[keyof typeof Currency];

export default function RegisterPage() {
  const { register, clearError, error: authError, isLoading } = useAuth();
  const navigate = useNavigate();
  const [isSuccess, setIsSuccess] = useState(false);

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    username: "",
    password: "",
    confirmPassword: "",
    riskTolerance: "MODERATE" as RiskToleranceType,
    securityQuestion: "MOTHER_MAIDEN_NAME" as SecurityQuestionType,
    securityAnswer: "",
    accountCurrency: "USD" as CurrencyType,
    initialVirtualBalance: 10000,
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const validateForm = () => {
    const newErrors: { [key: string]: string } = {};

    if (!formData.firstName || formData.firstName.length < 2)
      newErrors.firstName = "First name must be at least 2 characters";
    if (!formData.lastName || formData.lastName.length < 2)
      newErrors.lastName = "Last name must be at least 2 characters";
    if (!formData.email || !/\S+@\S+\.\S+/.test(formData.email))
      newErrors.email = "Valid email is required";
    if (!formData.username || formData.username.length < 3)
      newErrors.username = "Username must be at least 3 characters";
    if (!formData.password || formData.password.length < 8)
      newErrors.password = "Password must be at least 8 characters";
    if (formData.password !== formData.confirmPassword)
      newErrors.confirmPassword = "Passwords do not match";
    if (!formData.securityAnswer)
      newErrors.securityAnswer = "Security answer is required";
    if (
      formData.initialVirtualBalance < 1000 ||
      formData.initialVirtualBalance > 100000
    )
      newErrors.initialVirtualBalance =
        "Initial balance must be between 1,000 and 100,000";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "initialVirtualBalance" ? Number(value) : value,
    }));
    
    // Clear errors when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    
    // Clear auth errors when user starts typing
    if (authError) {
      clearError();
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      // Prepare data to match backend DTO structure
      const registrationData = {
        first_name: formData.firstName,
        last_name: formData.lastName,
        email: formData.email,
        username: formData.username,
        password: formData.password,
        risk_tolerance: formData.riskTolerance,
        security_question: formData.securityQuestion,
        security_answer: formData.securityAnswer,
        account_currency: formData.accountCurrency,
        initial_virtual_balance: formData.initialVirtualBalance,
      };

      console.log("Submitting registration with data:", {
        ...registrationData,
        password: "[HIDDEN]"
      });

      await register(registrationData);
      
      // Registration successful - automatically navigate to dashboard
      console.log("Registration successful, navigating to dashboard");
      navigate("/dashboard", { replace: true });
      
    } catch (error) {
      console.error("Registration failed:", error);
      
      let errorMessage = "Registration failed. Please try again.";
      
      if (error instanceof Error) {
        if (error.message.includes("username") && error.message.includes("already")) {
          errorMessage = "Username is already taken. Please choose a different one.";
        } else if (error.message.includes("email") && error.message.includes("already")) {
          errorMessage = "Email is already registered. Please use a different email or try logging in.";
        } else if (error.message) {
          errorMessage = error.message;
        }
      }
      
      setErrors({ submit: errorMessage });
    }
  };

  const getSecurityQuestionLabel = (question: SecurityQuestionType): string => {
    const labels = {
      MOTHER_MAIDEN_NAME: "What is your mother's maiden name?",
      FIRST_PET_NAME: "What was the name of your first pet?",
      HIGH_SCHOOL_NAME: "What is the name of your high school?"
    };
    return labels[question];
  };

  // Use auth error if available, otherwise use local error
  const displayError = authError || errors.submit;

  return (
    <div className="min-h-screen flex flex-col relative">
      {/* Background Image */}
<div 
  className="absolute inset-0 bg-cover bg-center bg-no-repeat"
  style={{
    backgroundImage: `url('./reg.jpg')`
  }}
>
  {/* Dark overlay for better text readability */}
  <div className="absolute inset-0 bg-black/50"></div>
</div>

{/* Content with relative positioning to appear above background */}
<div className="relative z-10 flex-1 flex flex-col">
      {/* Header */}
      
      <div className="relative z-10 text-center py-8">
        <h1 className="text-4xl font-bold text-white mb-2 drop-shadow-lg">SimInvest Platform</h1>
<p className="text-gray-200 text-lg drop-shadow">Investment Simulation & Learning</p>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center p-4">
        <form
          onSubmit={handleSubmit}
         className="bg-black/85 backdrop-blur-sm border border-gray-700 p-8 rounded-2xl shadow-2xl w-full max-w-2xl space-y-6"
        >
          <h2 className="text-2xl font-bold text-white text-center">
            Create Your Investment Account
          </h2>
          
          {displayError && (
            <div className="bg-red-500/20 border border-red-500/50 text-red-200 px-4 py-3 rounded-lg text-sm">
              {displayError}
            </div>
          )}

          {/* Personal Information */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="First Name"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.firstName && (
                <p className="text-red-400 text-sm mt-1">{errors.firstName}</p>
              )}
            </div>

            <div>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Last Name"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.lastName && (
                <p className="text-red-400 text-sm mt-1">{errors.lastName}</p>
              )}
            </div>
          </div>

          {/* Account Credentials */}
          <div className="space-y-4">
            <div>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Email Address"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.email && (
                <p className="text-red-400 text-sm mt-1">{errors.email}</p>
              )}
            </div>

            <div>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Username (3-20 characters)"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.username && (
                <p className="text-red-400 text-sm mt-1">{errors.username}</p>
              )}
            </div>

            <div>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Password (minimum 8 characters)"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.password && (
                <p className="text-red-400 text-sm mt-1">{errors.password}</p>
              )}
            </div>

            <div>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Confirm Password"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.confirmPassword && (
                <p className="text-red-400 text-sm mt-1">{errors.confirmPassword}</p>
              )}
            </div>
          </div>

          {/* Investment Preferences */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-300 mb-2">
                Risk Tolerance
              </label>
              <select
                name="riskTolerance"
                value={formData.riskTolerance}
                onChange={handleChange}
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              >
                <option value="CONSERVATIVE">Conservative (Low Risk)</option>
                <option value="MODERATE">Balanced (Medium Risk)</option>
                <option value="AGGRESSIVE">Aggressive (High Risk)</option>
              </select>
            </div>

            <div>
              <label className="block text-sm text-gray-300 mb-2">
                Account Currency
              </label>
              <select
                name="accountCurrency"
                value={formData.accountCurrency}
                onChange={handleChange}
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              >
                <option value="USD">USD - US Dollar</option>
              </select>
            </div>
          </div>

          {/* Initial Virtual Balance */}
          <div>
            <label className="block text-sm text-gray-300 mb-2">
              Initial Virtual Balance ({formData.accountCurrency})
            </label>
            <input
              type="number"
              name="initialVirtualBalance"
              value={formData.initialVirtualBalance}
              onChange={handleChange}
              min="1000"
              max="100000"
              step="100"
              className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
              disabled={isLoading}
            />
            <p className="text-xs text-gray-400 mt-1">
              Choose between $1,000 and $100,000 for your virtual trading account
            </p>
            {errors.initialVirtualBalance && (
              <p className="text-red-400 text-sm mt-1">
                {errors.initialVirtualBalance}
              </p>
            )}
          </div>

          {/* Security Question */}
          <div className="space-y-4">
            <div>
              <label className="block text-sm text-gray-300 mb-2">
                Security Question
              </label>
              <select
                name="securityQuestion"
                value={formData.securityQuestion}
                onChange={handleChange}
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              >
                {Object.values(SecurityQuestion).map((question) => (
                  <option key={question} value={question}>
                    {getSecurityQuestionLabel(question)}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <input
                type="text"
                name="securityAnswer"
                value={formData.securityAnswer}
                onChange={handleChange}
                placeholder="Security Answer"
                className="w-full px-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
                disabled={isLoading}
              />
              {errors.securityAnswer && (
                <p className="text-red-400 text-sm mt-1">{errors.securityAnswer}</p>
              )}
            </div>
          </div>

          <div className="flex items-center justify-between text-sm text-gray-300 pt-4">
            <Link 
              to="/login" 
              className="text-blue-400 hover:underline hover:text-blue-300 transition-colors"
            >
              Already have an account? Sign In
            </Link>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full bg-green-600 hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200"
          >
            {isLoading ? (
              <span className="flex items-center justify-center">
                <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Creating Account...
              </span>
            ) : (
              "Create Investment Account"
            )}
          </button>
        </form>
      </div>
      </div>


      {/* Footer */}
      <footer className="relative z-10 bg-black/80 backdrop-blur-sm border-t border-gray-700 py-6 px-4">
        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="text-center md:text-left">
              <p className="text-gray-400 text-sm">
                © 2025 Rodney Mbaguta. All rights reserved.
              </p>
            </div>
            <div className="flex flex-wrap justify-center md:justify-end gap-6 text-sm">
              <Link to="/terms" className="text-gray-400 hover:text-white transition-colors">
                Terms of Service
              </Link>
              <Link to="/privacy" className="text-gray-400 hover:text-white transition-colors">
                Privacy Policy
              </Link>
              <Link to="/contact" className="text-gray-400 hover:text-white transition-colors">
                Contact Us
              </Link>
              <Link to="/help" className="text-gray-400 hover:text-white transition-colors">
                Help Center
              </Link>
            </div>
          </div>
          <div className="mt-4 text-center">
            <p className="text-xs text-gray-500">
              SimInvest Platform - Educational investment simulation for learning purposes only. 
              Not real investment advice.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}