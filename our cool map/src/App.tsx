
import React from 'react';
import './App.css';
import MapApp from './MapApp';

function App() {
  return (
    <div className="App">
      <p className="App-header"><big>
        Welcome to our interactive map program!
        <p><small>
        An application by Dylan Lee and Sam Minars
        </small></p>
        </big></p>
      <p>
      This map displays historical redlining data in color on an interactive map.
      <br></br>
      Instructions: Click and drag anywhere on the map to pan around! Scroll to zoom in and out.
      <br></br>
        Click on any colored region of the map to access the State, City, and Name of the clicked region.
      </p>
      <MapApp />      
    </div>
  );
}

export default App;
