# Integration - Maps

### Samantha Minars (sminars) & Dylan Lee (dlee197)

### Estimated Time to Complete Project: 8 hrs

### Add link to repo

## How to Run & Intereact with Our Program
 - First navigate to our ```Server``` and run the ```main``` method to start the backend server. 
 - Then, navigate to a web brower and type ```localhost:133```. This should show a ```404 Not Found``` response in the window, which confirms that you successfully connected to the server, but there is no registered response at that endpoint. To access the redlining GeoJSON dataset, use the ```getredlinedata``` endpoint and provide a bounding box using the ```latmin```, ```latmax```, ```lonmin```, and ```lonmax``` parameters. 
    - Example: ```latmin```
- Potential Error Responses 

 - To open our map web-app in the browser window, navigate to the ```our cool map``` directory in a terminal and run ```npm start```. The server must be actively running to ensure that the redlining data is visible on the map. Our web-app displays map of the historical redlining data for major cities in the United States.
 -  How to interact with the Map:
    - Click and drag using the mouse to navigate to different regions on the map.
    - Scroll to zoom in and out. 
    - Click on any any area in the redlining overlay (i.e. the colored regions) to access the state, city, and name of the area if it's defined in the data. 
  
## **Design Choices**
### Frontend:
  - The frontend of our project is organized with an App class that sets up the basic strucute of our web-app and renders our MapApp to display an interactive map. 
  - 

### Backend
  - The backend of our project
  - ```RedLineHandler``` 


### Errors/Bugs: 
None
