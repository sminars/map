import './MapApp.css';
import React, { useState, Dispatch, SetStateAction, useEffect, useRef} from 'react';
import Map, {MapRef, ViewState, ViewStateChangeEvent, MapLayerMouseEvent, Source, Layer} from "react-map-gl"
import 'mapbox-gl/dist/mapbox-gl.css'
import { FeatureCollection } from "geojson";
import { FillLayer} from "react-map-gl";

import {myKey} from './private/key'
import mapboxgl, { PointLike } from 'mapbox-gl';

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

async function getGeoData(): Promise<string>{
  return await fetch("http://localhost:133/getredlinedata?latmin=-90&latmax=90&lonmin=-180&lonmax=180").then(response => 
  response.json().then(json => json.response.data))

}

function overlayData(response: any): GeoJSON.FeatureCollection | undefined{
    if(isFeatureCollection(response)){
      return response
    }
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


function onMapClick(ev: MapLayerMouseEvent, mapref: React.RefObject<MapRef>){
  console.log(ev.lngLat.lat)
  console.log(ev.lngLat.lng)
  const bbox: [PointLike, PointLike] = [
    [ev.point.x - 5, ev.point.y - 5],
    [ev.point.x + 5, ev.point.y + 5]
];
  let statestring: string | undefined;
  let citystring: string | undefined;
  let namestring: string | undefined;
  if(mapref.current != null){
    const selectedFeatures = mapref.current.queryRenderedFeatures(bbox);
    //Now from here, all we have to do is figure out the state, city and name of the place we've selected
    console.log(selectedFeatures);
    //TODO: Go through these and get the city, state, and name of the area clicked(IF DEFINED). Note that the properties may be null.
    //We care about something in selectedFeatures if and only if its layer has id: "geo-data". So do a filter.
    for(let i = 0; i < selectedFeatures.length; i++){
      let feature: mapboxgl.MapboxGeoJSONFeature = selectedFeatures[i];
      if(feature.properties != null){
        if(statestring == undefined){
          statestring = feature.properties.state
        }
        if(citystring == undefined){
          citystring = feature.properties.city
        }
        if(namestring == undefined){
          namestring = feature.properties.name
        }
          //So these might be undefined. Only update statestring and the other parts if it's undefined
      }
    }
    console.log("state: " + statestring)
    console.log("city: " + citystring)
    console.log("name: " + namestring)
  }
}

export default function MapApp() {
  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(undefined);
  const mapRef = useRef<MapRef>(null)

  useEffect(() => {

    const getoverlay = async () => {
      const response = await fetch("http://localhost:133/getredlinedata");
      const json = await response.json();
      const data = overlayData(json.response.data);
      setOverlay(data);
      }
    getoverlay().catch(console.error);;
    }, []);

  const [viewState, setViewState] = React.useState({
    latitude: 41.8258,
    longitude: -71.4029,
    zoom: 10
  }); 
  return (
    <div className="App"> 
      <Map
          ref={mapRef} 
          longitude = {viewState.longitude}
          mapboxAccessToken = {myKey}
          latitude = {viewState.latitude}
          zoom = {viewState.zoom}
          onMove={(ev: ViewStateChangeEvent)=> setViewState(ev.viewState)}
          onClick={(ev: MapLayerMouseEvent) => onMapClick(ev,mapRef)}
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
