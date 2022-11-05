import './Puzzle.css';
import React, { useState, Dispatch, SetStateAction, useEffect } from 'react';
import Map, {ViewState, ViewStateChangeEvent, MapLayerMouseEvent, Source, Layer} from "react-map-gl"
import 'mapbox-gl/dist/mapbox-gl.css'
import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

import rl_data from "./private/fullDownload.json"

import {myKey} from './private/key'

// When we write tests, we'll be searching using accessible names. So let's
// use the same constant identifier; that way if we decide to change the text
// in the app, it won't break our tests.
export const TEXT_try_button_accessible_name = 'try your sequence'
export const TEXT_number_1_accessible_name = 'first number in sequence'
export const TEXT_number_2_accessible_name = 'second number in sequence'
export const TEXT_number_3_accessible_name = 'third number in sequence'
export const TEXT_try_button_text =  'Try it!'

function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection"
}

function overlayData(): GeoJSON.FeatureCollection | undefined {
  if(isFeatureCollection(rl_data))
    return rl_data
  return undefined
}


const propertyName = 'holc_grade';
const geoLayer: FillLayer = {
  id: 'geo_data',
  type: 'fill',
  paint: {
    'fill-color': [
      'match',
      ['get', propertyName],
      'A',
      '#5bcc04',
      'B',
      '#04b8cc',
      'C',
      '#e9ed0e',
      'D',
      '#d11d1d',
      /** */ '#ccc'
    ],
    'fill-opacity': 0.2
  }
}

// Remember that parameter names don't necessarily need to overlap;
// I could use different variable names in the actual function.
interface ControlledInputProps {
  value: string, 
  // This type comes from React+TypeScript. VSCode can suggest these.
  //   Concretely, this means "a function that sets a state containing a string"
  setValue: Dispatch<SetStateAction<string>>,
  ariaLabel: string 
}

// Input boxes contain state. We want to make sure React is managing that state,
//   so we have a special component that wraps the input box.
function ControlledInput({value, setValue, ariaLabel}: ControlledInputProps) {
  return (
    <input value={value} 
           onChange={(ev) => setValue(ev.target.value)}
           aria-label={ariaLabel}
           ></input>
  );
}


function onMapClick(e: MapLayerMouseEvent){
  console.log(e)
}



export default function Puzzle() {
  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(undefined);

  useEffect(() => {
    setOverlay(overlayData);
  }, []);

  const [viewState, setViewState] = React.useState({
    latitude: 41.8258,
    longitude: -71.4029,
    zoom: 10
  });
  return (
    <div className="App"> 
      <Map longitude = {viewState.longitude}
          mapboxAccessToken = {myKey}
          latitude = {viewState.latitude}
          zoom = {viewState.zoom}
          onMove={(ev: ViewStateChangeEvent)=> setViewState(ev.viewState)}
          style={{width: window.innerWidth, height: window.innerHeight}}
          mapStyle={'mapbox://styles/mapbox/light-v10'}>
        <Source
            id = "geo_data"
            type = "geojson"
            data={overlay}
            >
            <Layer {...geoLayer} />
          </Source>
      </Map>
    
    </div>
  );
}
