import React from 'react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Legend, Tooltip } from 'recharts';

// Mock data to display while real data is loading or for testing
const mockData = [
    { subject: 'Knowledge', count: 40, fullMark: 100 },
    { subject: 'Entertainment', count: 70, fullMark: 100 },
    { subject: 'Lifestyle', count: 85, fullMark: 100 },
    { subject: 'Art/Music', count: 60, fullMark: 100 },
    { subject: 'Self-Dev', count: 30, fullMark: 100 },
    { subject: 'Social', count: 55, fullMark: 100 },
];

const RadarChartComponent = ({ analysisData }) => {

    // Format the data from our backend to fit what recharts expects
    const processData = (data) => {
        if (!data || Object.values(data).every(v => v === 0)) return mockData; // Return mock data if no real data or all values are zero

        const maxValue = Math.max(...Object.values(data));

        return Object.keys(data).map(key => ({
            subject: key.replace('/', '/\n'), // Add newline for long labels
            count: data[key],
            fullMark: maxValue > 20 ? maxValue : 20, // Ensure the chart has a decent scale
        }));
    };

    const chartData = processData(analysisData);

    return (
        <ResponsiveContainer width="100%" height={350}>
            <RadarChart cx="50%" cy="50%" outerRadius="80%" data={chartData}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" />
                <PolarRadiusAxis angle={30} domain={[0, 'dataMax + 5']} />
                <Radar name="Videos Watched" dataKey="count" stroke="#8884d8" fill="#8884d8" fillOpacity={0.6} />
                <Legend />
                <Tooltip />
            </RadarChart>
        </ResponsiveContainer>
    );
};

export default RadarChartComponent;
