#SPRINT 2: API PROXY

##Project description:
For this project, we created a web API that can load and retrieve CSV files from a local “data” 
folder on the user’s computer, as well as return the temperature in Fahrenheit at a specific 
latitude and longitude.
Team members: dlee197 and hadrian
Contributors: isung2
Total estimated time: 10~12 hours
Repository: (https://github.com/cs0320-f2022/sprint-2-dlee197-hadrian)

##Design choices:
For this project, we decided to create a main package, which houses three sub-packages: server, csv, 
and weather. The server class contains a single class called “Server”, which sets up our Spark port 
and all of the query handlers. The csv package contains a host of classes and interfaces directly 
copied from our CSVParser assignment(sprint 0), a LoadCSVHandler class, as well as a sub-package 
called “data” which houses our CSV data files. Finally, our weather package holds a WeatherHandler 
class, as well as a sub-package called “deserializerclasses” that holds all of the classes we 
created to deserialize the response received from the national weather service API that we use for 
weather queries. The classes in the deserializerclasses package form two nested classes, one called 
“Weather” and one called “Forecast”. Their structures look like this(truncated for simplicity):

`Weather -> WeatherProperties -> String: forecast`
`Forecast -> ForecastProperties -> List<Periods>`
`Periods -> Integer: temperature`

When we send our first request to the national weather service API, we receive a response in JSON 
format, which we then deserialize using our first nested “Weather” class, which returns a Weather 
object. From there, we extract the “forecast” url string and make a request to that URL, and then 
we take the JSON response from that URL and again deserialize it, but this time using our “Forecast”
class, which will return a Forecast object that stores the list of temperatures throughout the week.

##Data structures:
Since the reader objects in our program can technically read from any filepath provided by a user, 
this opens up a massive security flaw. Additionally, whenever we run loadCSV on an arbitrary CSV 
file, we do not immediately know whether or not that CSV file has a header or not, which is 
important for our CSV parser to function. To address both of these issues at once, we created a 
HashMap from Strings to Booleans called `validpaths` in our Server class, which contains a set of 
valid file paths as the String keys, and whether or not they have a header as the Boolean value. 
Whenever a user makes a loadCSV request to the API server, LoadCSVHandler will check to see if the 
file path provided in the load query is a valid key in `validpaths`.
Another data structure of note is the `replies` HashMap from Strings to Objects that we use to send 
responses through the API server. Depending on whether or not a request is formatted properly or 
returns a successful response, we either call `replies.put(“result, “success”)` followed by 
`replies.put(<response object type>, <response object>)`, or, if the query is ill-formed or the 
response fails, we call `replies.put(“result”, <error message>)`.
Once our CSV data has been loaded by the API, we store it as a list of lists under the name 
`currCSVData` in our main server class so it can be easily accessed by our GetCsvHandler class.

##Errors/Bugs:
In general, if a user attempts to reach an unreachable endpoint in the API, it will respond with a 
404 not found error. If a user calls “loadcsv” and provides an invalid filepath(one not specified 
in the validpaths map), our program returns a `error_datasource` error. If a user calls `loadcsv` 
on a file that is not in CSV format, we return an `error_bad_json` error. If a user calls `getcsv` 
with a nonzero number of query parameters, our program returns a `error_bad_request` error since 
`getcsv` should take in no parameters. Similarly, if a user calls `getcsv` when no csv has been 
loaded, the program returns an `error_datasource` error. As for weather requests, if we receive a 
request that does not contain exactly one `lat` query parameter and one `lon` query parameter, or if
those query parameters are empty, we raise an `error_bad_request` error. Furthermore, if any of our 
`deserialize()` methods in the WeatherHandler class fail, we catch the exception and print a 
`StackTrace()` of the issue. Finally, if the user requests the temperature at a coordinate that 
either does not exist in the National Weather Service Database, or is simply outside the bounds of 
proper latitude and longitude coordinates, we raise an `error_bad_request` error.
While coding this project, we discovered some smaller bugs/points of interest. In particular, we 
discovered that if one makes a request to the National Weather Service API asking for the weather 
at some pair of coordinates which contain more than four decimal digits, the NWS API will actually 
return an error message in its immediate response, and attempting to deserialize this response the 
way our program deserializes forecast responses causes the entire program to crash, burn, and halt 
without throwing any errors- it simply gets stuck. The issue with this rounding error is that it is
not immediately apparent when testing the NWS API in a browser, as the browser subtly sends the user 
straight to the response it would have given for that response rounded to four decimal places. To 
deal with this, my partner and I simply built in a method that takes floats and rounds them to 4 
decimal places. We also noticed during testing that if one attempts to run `tryRequest(“/getcsv”)` 
immediately after running `tryRequest(“/loadcsv?filepath=<filepath>”)`, the getcsv request will 
return null. For whatever reason, the `tryRequest(“/loadcsv?filepath=<filepath>”)` line does not
actually execute the request until one calls `.getResponseCode()` on it afterwards. We quickly 
patched this as well.

##Tests:
In our testing suite, we have two main testing files, TestHandlers and TestWeather. TestHandlers 
tests our CSV loader and getter endpoints, and is comprised of five tests. The first test tests to 
see if calling `loadcsv` on our mockdata1 CSV file gives a valid connection with a ResponseCode of 
200, then tests to see if the actual response returned from the server is expected, then runs the 
same process on mockdata2. Our second test checks if calling `loadcsv` on an invalid filepath 
results in an `error_datasource` error, and the third test checks if calling `loadcsv` on an empty 
filepath results in the same error_datasource error. The fourth test checks if we can successfully 
retrieve the contents of our mockdata1 CSV file via `getcsv` after loading that same file, and 
checks the response from the API against our expected response. Finally, our last test checks to 
see that our program raises an error_datasource error if we call `getcsv` without loading a csv 
file first.
Our TestWeather testing suite has four main tests. The first test checks to see if making a weather 
request for a valid set of latitude and longitude coordinates results in a success response from the
API, and the second test checks to see if making a weather request with coordinates not contained in
the NWS API database(ex: the middle of nowhere in the pacific ocean) returns an `error_bad_request` 
message. The third test checks to see if requesting the weather at coordinates out of the bounds of 
standard latitude and longitude format(from -90 to 90 and from -180 to 180 in value) also returns an
`error_bad_request` message. Finally, the fourth test checks to see if making a request with two 
parameters of the wrong type(aka, `red=50&blue = 200`) results in an `error_bad_request` message.

##How to…
###Running the API:
To use our program, one must clone the github repository to their local computer, open the main 
“sprint-2-dlee197-hadrian” folder in IntelliJ, and then navigate to the “Server” class via:
`sprint-2-dlee197-hadrian -> src -> main -> server -> Server`.
From here, one can click the green play button that appears to the left of the
“public class Server” line at line 21, which will create an interactive API server at the URL 
(http://localhost:133).
From here, one can either choose to load a csv file, get a csv file, or request the current 
temperature in any latitude or longitude.
To get the data of a csv file, one must first load the file by visiting the following URL:
(http://localhost:133/loadcsv?filepath=<filepath>).
In the `<filepath>` field, one may provide one of two filepaths which correspond to the two CSV 
files stored in the
`src-> main -> csv -> data`
folder of the project:
src/main/csv/data/mockdata1.csv, and
src/main/csv/data/mockdata2.csv.
After the file has been loaded, one should receive a success response from the API. One can then 
visit the following URL to read the contents of the last loaded CSV file by visiting:
(http://localhost:133/getcsv).
If one has not loaded a CSV file at any time prior to calling `getcsv`, then the API will return an 
error message. Otherwise, the API will respond with a success response and the contents of the CSV 
file.
Finally, one can check the current temperature at any pair of coordinates by visiting
(http://localhost:133/weather?lat=<latitude>&lon=<longitude>), where `<latitude>` and `<longitude>` 
are the coordinates of the destination where one would like to know the current temperature. Our API
rounds the numbers provided in the `<latitude>` and `<longitude>` fields down to 4 decimal places 
due to the format the National Weather Service API requires for requests. If a valid latitude and 
longitude was provided, our API will return a success response containing the current temperature at
that location.
###Running tests:
To run our tests, one can simply navigate to the folder
`src -> main -> tests`
and then open either the TestHandlers or TestWeather class. Scrolling to the very top of the class 
and clicking the double green play arrows to the left of the line beginning with “public class” will
run all the tests in that suite.
###Adding new files:
If one would like to add a new loadable csv file to the project, they can simply drop the CSV file 
into the “data” package within the project, then go to the main “Server” class and add a line after 
the declaration of `validpaths` of the form:
`validpaths.put(<filepath>,<hasHeader>)`, where `<filepath>` is a string denoting the filepath from 
content route of the new CSV file, and `<hasHeader>` is a boolean of form `true` or `false` 
depending on whether or not the CSV file provided contains a first row header. It is very important 
that the user follows this step pertaining to `validpaths`, because otherwise the backend program 
will not recognize the CSV filepath as a valid path.
###Adding new endpoints:
If a user would like to add a new data-fetching endpoint to the API server, all they have to do is 
navigate to the “Server” class and scroll down to the `Spark.get()` lines, and add a new line 
underneath of the form:
`Spark.get(<path>, new <Route>)`, where the `<path>` field is the string representing the desired 
endpoint, while <Route> is an instance of a new class that implements the Route interface. We also 
provide a generalized SerializeExternalData class that allows developers to easily serialize any 
data source provided to the class. We originally considered creating a general “FetchDataHandler” 
class that takes in a class that implements some general “retriever” method for processing 
responses, but came up short on time, especially considering that we would only be wrapping the 
WeatherHandler class in the FetchDataHandler class(as the get/load commands are a two step process).