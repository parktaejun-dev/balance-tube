import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import RadarChartComponent from '../components/RadarChartComponent';
import Recommendations from '../components/Recommendations';

// Configure axios to communicate with our backend
const apiClient = axios.create({
    baseURL: 'http://localhost:3001',
});

const DashboardPage = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState({ id: 1 }); // Hardcoded user for MVP
    const [analysisData, setAnalysisData] = useState(null);
    const [recommendations, setRecommendations] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSyncing, setIsSyncing] = useState(false);

    const fetchAnalysisData = useCallback(async () => {
        if (!user) return;
        try {
            const response = await apiClient.get(`/api/analysis/result/${user.id}`);
            setAnalysisData(response.data);
        } catch (error) {
            console.error('Error fetching analysis data:', error);
        }
    }, [user]);

    const fetchRecommendations = useCallback(async () => {
        if (!user) return;
        try {
            const response = await apiClient.get(`/api/recommendations/${user.id}`);
            setRecommendations(response.data);
        } catch (error) {
            console.error('Error fetching recommendations:', error);
        }
    }, [user]);


    useEffect(() => {
        const queryParams = new URLSearchParams(window.location.search);
        if (queryParams.get('login_success') === 'false') {
            alert('Login failed. Please try again.');
            navigate('/');
            return;
        }

        const fetchData = async () => {
            setIsLoading(true);
            await fetchAnalysisData();
            await fetchRecommendations();
            setIsLoading(false);
        };

        fetchData();
    }, [navigate, fetchAnalysisData, fetchRecommendations]);

    const handleSync = async () => {
        if (!user) {
            alert('User not found. Please log in again.');
            return;
        }
        setIsSyncing(true);
        try {
            // In a real app, tokens would be stored securely (e.g., httpOnly cookie)
            // and sent automatically. Here, we'd need a way to get them after login.
            // This part of the flow is simplified for the MVP.
            alert("This flow is simplified for MVP. We'll simulate the process.");

            // 1. Simulate fetching youtube history & passing tokens
            // const historyResponse = await apiClient.post('/api/youtube/history', { tokens: 'DUMMY_TOKENS' });

            // 2. Simulate running the analysis
            // await apiClient.post('/api/analysis/run', { userId: user.id, videos: historyResponse.data });

            // For now, we'll just re-fetch the data after a delay
            setTimeout(async () => {
                await fetchAnalysisData();
                await fetchRecommendations();
                alert('Sync and analysis complete!');
                setIsSyncing(false);
            }, 2000);

        } catch (error) {
            console.error('Error syncing history:', error);
            alert('Failed to sync history.');
            setIsSyncing(false);
        }
    };

    if (isLoading) {
        return <div style={{ textAlign: 'center', marginTop: '50px' }}>Loading Dashboard...</div>;
    }

    return (
        <div style={{ padding: '20px', maxWidth: '1200px', margin: 'auto' }}>
            <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2>BalanceTube Dashboard</h2>
                <button onClick={handleSync} disabled={isSyncing}>
                    {isSyncing ? 'Syncing...' : 'Sync Watch History'}
                </button>
            </header>

            <main style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
                <section>
                    <h3>Your Content Balance</h3>
                    <RadarChartComponent analysisData={analysisData} />
                </section>
                <section>
                    <h3>Recommendations</h3>
                    <Recommendations recommendations={recommendations?.recommendations} category={recommendations?.leastWatchedCategory} />
                </section>
            </main>
        </div>
    );
};

export default DashboardPage;
