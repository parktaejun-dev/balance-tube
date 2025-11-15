import React from 'react';

const LoginPage = () => {

    const handleLogin = () => {
        // Redirect the user to the backend's Google authentication route
        window.location.href = 'http://localhost:3001/auth/google';
    };

    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', textAlign: 'center' }}>
            <h1>Welcome to BalanceTube</h1>
            <p>Analyze your YouTube watch history to find your content balance.</p>
            <button onClick={handleLogin} style={{ marginTop: '20px', padding: '10px 20px', fontSize: '16px', cursor: 'pointer' }}>
                Sign in with Google to Get Started
            </button>
        </div>
    );
};

export default LoginPage;
