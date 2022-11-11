import './MapApp.css';
import React, { useState, Dispatch, SetStateAction, useEffect, useRef} from 'react';
import Map, {MapRef, ViewState, ViewStateChangeEvent, MapLayerMouseEvent, Source, Layer} from "react-map-gl"
import 'mapbox-gl/dist/mapbox-gl.css'
import { FeatureCollection } from "geojson";
import { FillLayer} from "react-map-gl";

import {myKey} from './private/key'
import mapboxgl, { PointLike } from 'mapbox-gl';

// A function we use check if any object is a FeatureCollection
function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection"
}

// A function we use to check if an object is a FeatureCollection
function overlayData(response: any): GeoJSON.FeatureCollection | undefined{
    if(isFeatureCollection(response)){
      return response
    }
    return undefined
  }

// General formatting/style constants
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

// A function that takes in a map click event and the current state of the map and returns
// an array of strings containing the state, city, and name of the feature clicked
function onMapClick(ev: MapLayerMouseEvent, mapref: React.RefObject<MapRef>): String[]{
  console.log("latitude and longitude: " + ev.lngLat.lat + ", " + ev.lngLat.lng)
  // Construct the bounding box we'll use for filtering onscreen features
  const bbox: [PointLike, PointLike] = [
    [ev.point.x - 5, ev.point.y - 5],
    [ev.point.x + 5, ev.point.y + 5]
];
  // General constants we use for our return statement
  let statestring: string | undefined;
  let citystring: string | undefined;
  let namestring: string | undefined;
  let returnArray = ['','',''];

  // Map could be null, we just do a simple check first
  if(mapref.current != null){
    // Filter to just the selected features
    const selectedFeatures = mapref.current.queryRenderedFeatures(bbox);
    // Iterate through each of the features and set the state, city, and name to be the first non-null value
    // we encounter. 
    for(let i = 0; i < selectedFeatures.length; i++){
      let feature: mapboxgl.MapboxGeoJSONFeature = selectedFeatures[i];
      if(feature.properties != null){
        // Only change the value if the strings are undefined. If they aren't undefined then we already set them to
        // a value for some earlier feature
        if(statestring == undefined){
          statestring = feature.properties.state
        }
        if(citystring == undefined){
          citystring = feature.properties.city
        }
        if(namestring == undefined){
          namestring = feature.properties.name
        }
      }
    }
    // Check if the state, city, and name are undefined. If so, N/A, otherwise just return whatever string we found.
    // The state, city, and name of the feature clicked are stored in the array in that order.
    if(statestring != undefined){
      console.log("state: " + statestring)
      returnArray[0] = statestring
    }
    else{
      console.log("state: could not find state information")
      returnArray[0] = 'N/A'
    }
    if(citystring != undefined){
      console.log("city: " + citystring)
      returnArray[1] = citystring
    }
    else{
      console.log("city: could not find city information")
      returnArray[1] = 'N/A'
    }
    if(namestring != undefined){
      console.log("name: " + namestring)
      returnArray[2] = namestring
    }
    else{
      console.log("could not find name information")
      returnArray[2] = 'N/A'
    }
  }
  // Return the array
  return returnArray
}

export default function MapApp() {
  // We need to access the overlay data, the string that appears onscreen when we click, and the current state of the map.
  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(undefined);
  const [clickstring, setClickstring] = useState(' ')
  const mapRef = useRef<MapRef>(null)

  useEffect(() => {
    // Getting the overlay data using our API server
    const getOverlay = async () => {
      const response = await fetch("http://localhost:133/getredlinedata");
      const json = await response.json();
      const data = overlayData(json.response.data);
      setOverlay(data);
      }
    getOverlay().catch(console.error);;
    }, []);

  // Making it so we can store and move around our viewpoint on the map
  const [viewState, setViewState] = React.useState({
    latitude: 41.8258,
    longitude: -71.4029,
    zoom: 10
  }); 

  return (
    <div className="App"> 
      <pre>
        {clickstring}
      </pre>
      <Map
          ref={mapRef} 
          longitude = {viewState.longitude}
          mapboxAccessToken = {myKey}
          latitude = {viewState.latitude}
          zoom = {viewState.zoom}
          onMove={(ev: ViewStateChangeEvent)=> setViewState(ev.viewState)}
          // Changing the displayed text when we click anywhere on the map
          onClick={(ev: MapLayerMouseEvent) => {let arr = onMapClick(ev,mapRef);
            setClickstring("Latitude: " + ev.lngLat.lat + ", Longitude: " + ev.lngLat.lng +
            " \nState: " + arr[0] + " City: " + arr[1] + " Name: " + arr[2])}}

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
