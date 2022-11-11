# Integration - Maps

### Samantha Minars (sminars) & Dylan Lee (dlee197)

### Estimated Time to Complete Project: 8 hrs

### Add link to repo

## How to Run our Program
  - First navigate to our ```Server``` and run the ```main``` method to start the backend server. 
  - Then, navigate to a web brower and type ```localhost:133```. This should show a ```404 Not Found``` response in the window, which confirms that you successfully connected to the server, but there is no registered response at that endpoint. To access the redlining GeoJSON dataset, use the ```getredlinedata``` endpoint and provide a bounding box using the ```latmin```, ```latmax```, ```lonmin```, and ```lonmax``` parameters. 
    Example: ```latmin```
  - To open our map web-app in the browser window, navigate to the ```our cool map``` directory and run ```npm start```. The server must be actively running to ensure that the redlining data is visible on the map.
  
## **Design Choices**
  - The frontend of our project is organized with an App class that sets up the basic strucute of our web-app and renders our MapApp to display an interactive map. 

  - The backend of our project
  - ```RedLineHandler``` 

### Errors/Bugs: 
None
