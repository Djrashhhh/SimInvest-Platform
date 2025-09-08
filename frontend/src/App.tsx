import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import { ProtectedRoute } from "./components/auth/ProtectedRoute";
import DashboardPage from "./pages/dashboard/DashboardPage";
import MarketPage from "./pages/dashboard/MarketPage";
import LoginPage from "./pages/auth/LoginPage";
import RegisterPage from "./pages/auth/RegisterPage";
import MarketNewsPage from "./pages/news/MarketNewsPage";
import WatchlistPage from "./pages/dashboard/WatchlistPage";
import OrdersTransactionsPage from './pages/dashboard/OrdersTransactionsPage';
import PortfolioPage from "./pages/dashboard/PortfolioPage";
import EducationalContentPage from "./pages/dashboard/EducationalContentPage";
import "./styles/globals.css";

// The main App component that defines the application's structure and routing.
function App() {
  return (
    // AuthProvider wraps the entire application to provide authentication context.
    <AuthProvider>
      {/* Router enables client-side routing. */}
      <Router>
        {/* Routes is a container for all Route components. */}
        <Routes>
          {/*
            This is the new route for the root path ('/').
            When the user navigates to the base URL, it will immediately
            redirect them to the /login page using the Navigate component.
          */}
          <Route path="/" element={<Navigate to="/login" />} />

          {/* Route for the login page. */}
          <Route path="/login" element={<LoginPage />} />

          {/* Route for the registration page. */}
          <Route path="/register" element={<RegisterPage />} />

          {/* Route for the dashboard. It is wrapped in ProtectedRoute,
            which will check for user authentication before rendering.
          */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />
          {/* New route for the orders & transactions page */}
          <Route
            path="/orders-transactions"
            element={
              <ProtectedRoute>
                <OrdersTransactionsPage />
              </ProtectedRoute>
            }
          />
          {/* New route for the market page */}
          <Route
            path="/market"
            element={
              <ProtectedRoute>
                <MarketPage />
              </ProtectedRoute>
            }
          />
          {/* NEW: Market News Route */}
          <Route
            path="/news"
            element={
              <ProtectedRoute>
                <MarketNewsPage />
              </ProtectedRoute>
            }/>
            {/* NEW: Watchlist Page Route */}
          <Route
            path="/watchlist"
            element={
              <ProtectedRoute>
                <WatchlistPage />
              </ProtectedRoute>
            }
          />
          {/* NEW: Portfolio Page Route */}
          <Route
            path="/portfolios"
            element={
              <ProtectedRoute>
                <PortfolioPage />
              </ProtectedRoute>
            }
          />
           {/* NEW: Ed content Page Route */}
          <Route
            path="/learning"
            element={
              <ProtectedRoute>
                <EducationalContentPage />
              </ProtectedRoute>
            }
          />
          
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
