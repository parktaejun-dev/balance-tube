import React, { useEffect, useState, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

import RadarChartComponent from '../components/RadarChartComponent';
import Recommendations from '../components/Recommendations';

const apiClient = axios.create({
    baseURL: 'http://localhost:3001',
});

const DashboardPage = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const [user, setUser] = useState(null);
    const [analysisData, setAnalysisData] = useState(null);
    const [recommendations, setRecommendations] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSyncing, setIsSyncing] = useState(false);

    const fetchAnalysisData = useCallback(async (userId) => {
        if (!userId) return;
        try {
            const response = await apiClient.get(`/api/analysis/result/${userId}`);
            setAnalysisData(response.data);
        } catch (error) {
            console.error('Error fetching analysis data:', error);
        }
    }, []);

    const fetchRecommendations = useCallback(async (userId) => {
        if (!userId) return;
        try {
            const response = await apiClient.get(`/api/recommendations/${userId}`);
            setRecommendations(response.data);
        } catch (error) {
            console.error('Error fetching recommendations:', error);
        }
    }, []);

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const userId = queryParams.get('userId');

        if (userId) {
            setUser({ id: userId });
            const fetchData = async () => {
                setIsLoading(true);
                await fetchAnalysisData(userId);
                await fetchRecommendations(userId);
                setIsLoading(false);
            };
            fetchData();
        } else {
            // If no userId, maybe redirect to login
            navigate('/');
        }
    }, [location.search, navigate, fetchAnalysisData, fetchRecommendations]);

    const handleSync = async () => {
        if (!user) {
            alert('User not found. Please log in again.');
            return;
        }
        setIsSyncing(true);
        try {
            // 1. Fetch user's actual YouTube watch history
            console.log("Step 1: Fetching YouTube history...");
            const historyResponse = await apiClient.get(`/api/youtube/history/${user.id}`);

            if (historyResponse.data.length === 0) {
                alert("Could not find any videos in your watch history.");
                setIsSyncing(false);
                return;
            }

            // 2. Send the history to the backend for analysis
            console.log("Step 3: Sending videos for analysis...");
            await apiClient.post('/api/analysis/run', {
                userId: user.id,
                videos: historyResponse.data
            });

            // 3. Re-fetch the analysis data and recommendations to update the UI
            console.log("Step 4: Fetching updated analysis and recommendations...");
            await fetchAnalysisData(user.id);
            await fetchRecommendations(user.id);

            alert('Sync and analysis complete! Your dashboard is updated.');

        } catch (error) {
            console.error('Error syncing history:', error);
            alert(`Failed to sync history: ${error.message}`);
        } finally {
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
