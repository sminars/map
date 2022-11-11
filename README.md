# Integration - Maps

### Samantha Minars (sminars) & Dylan Lee (dlee197)

### Estimated Time to Complete Project: 8 hrs

### [github repo](https://github.com/cs0320-f2022/integration-dlee197-sminars.git)

## How to Run & Intereact with Our Program
 - First navigate to our ```Server``` and run the ```main``` method to start the backend server. 
 - Then, navigate to a web brower and type ```localhost:133```. This should show a ```404 Not Found``` response in the window, which confirms that you successfully connected to the server, but there is no registered response at that endpoint. To access the redlining GeoJSON dataset, use the ```getredlinedata``` endpoint and provide a bounding box using the ```latmin```, ```latmax```, ```lonmin```, and ```lonmax``` parameters. 
    - Example: ```http://localhost:133/getredlinedata?latmin=-97&latmax=-96&lonmin=40.83&lonmax=41```
- A bad request error will occur when an API call is made with...
  - an invalid number of parameters (!=4)
  - a larger numerical value assigned to the minimum latitude than to the maximum latitude
  - a larger numerical value assigned to the minimum longitude than to the maximum longitude
  - latitude and longitude ranges where no redlining data is available


 - To open our map web-app in the browser window, navigate to the ```our cool map``` directory in a terminal and run ```npm start```. The server must be actively running to ensure that the redlining data is visible on the map. Our web-app displays map of the historical redlining data for major cities in the United States.
 -  How to interact with the Map:
    - Click and drag using the mouse to navigate to different regions on the map.
    - Scroll to zoom in and out. 
    - Click on any any area in the redlining overlay (i.e. the colored regions) to access the state, city, and name of the area if it's defined in the data. 
  
## Design Choices
### Frontend
  - The frontend of our project is organized with an App class that sets up the basic strucute of our web-app and renders our MapApp to display an interactive map. 
  - In MapApp, we overlay the redlining data by making a call to our backend API server inside an effect hook. 
  - To provide the state, city, and name of a clicked feature, we set-up a useRef hook and map click event to pass into our ```onMapClick``` function. This function first constructs a bounding box from the latitude and longitude values of the area that was clicked. This bounding box is then passed to the ```queryRenderedFeatures()``` method to return back all the features in that area. We create constants to represent the state, city, and name information. We then loop through all the features in the bounding box area to assign the first non-null value to each constant. Finally, we return an array that stores the stae, city, and name information.

### Backend
  - The backend of our project was adapted from Sprint 2. We added a ```getredlinedata``` endpoint to our server. 
  - Our ```RedLineHandler()``` returns the redlining data as a GeoJSON associated with the user provided bounding box. If no bounding box is provided, it will return all the GeoJSON data. 
  - The```RedLineHandler``` class takes care of deserialzing the redlining data from the fullDownload.json file into a ```GeoData``` object. We loop through all coordinates in the ```GeoData``` object to check if they are within the user provided latitude and longitude ranges. If each array of coordinates is within the bounding box, that feature is marked as valid. Once we have all the features within the bounding box, we create a new ```GeoData``` object and serilize it to return a redline success response. 

## Testing
- We felt that integration testing provided an ideal avenue to test the overall functionality of sending API requests to a server and outputting the correct responses.
In our ```TestRedlineHandler``` class, we set up a Spark server and get request for the redline hanlder. Each individual test method set up a HTTPConnection with an URL that tests a specific endpoint. We used assertEquals() to determine if an okay connection was established. We then used moshi to create a response to the URL. For testing purposes, we had to create ```RedlineResponse``` and ```ResponseClass``` to serialize a response from our API call. When testing successfully API calls, we used ```assertEquals()``` to test if the number of features equaled what we expected it to be. When we were testing smaller API calls, we also checked if the state, city, and name was equal to what we expected it to be. We also tested scenarios where an ```error_bad_request``` is thrown. We also tested that a response with empty fields is returned when an API call is made with an area where no redlining data is available. 


## Errors/Bugs
None to our knowledge!
