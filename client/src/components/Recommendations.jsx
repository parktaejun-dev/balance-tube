import React from 'react';

const Recommendations = ({ recommendations, category }) => {
    if (!recommendations) {
        return <p>Sync your history to get new recommendations.</p>;
    }

    if (recommendations.length === 0) {
        return <p>No recommendations found for your least watched category: <strong>{category}</strong></p>;
    }

    return (
        <div>
            <h4>Here are some videos from your least watched category: <strong>{category}</strong></h4>
            <ul style={{ listStyleType: 'none', padding: 0 }}>
                {recommendations.map(item => (
                    <li key={item.id.videoId} style={{ marginBottom: '10px', border: '1px solid #ccc', padding: '10px' }}>
                        <a href={`https://www.youtube.com/watch?v=${item.id.videoId}`} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none', color: 'inherit' }}>
                            <img src={item.snippet.thumbnails.default.url} alt={item.snippet.title} style={{ marginRight: '10px', verticalAlign: 'middle' }} />
                            <span>{item.snippet.title}</span>
                        </a>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default Recommendations;
