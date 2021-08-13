import java.io.*;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RoadTrip {
    public Integer totalCost=0;
    public HashMap<String, String> attractions;
    public ArrayList<Road> roadsList;

    public ArrayList<String> cityList;

    private List<Vertex> nodes;
    private List<Edge> edges;

    static Scanner scanner = new Scanner(System.in);


    public RoadTrip() {
        this.attractions = new HashMap<>();
        this.roadsList=new ArrayList<>();
        this.cityList=new ArrayList<>();
    }

    private void loadingData(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("attractions.csv")));
            String line;
            while((line = br.readLine()) != null)
                attractions.put(line.split(",")[0],line.split(",")[1]);
            attractions.remove("Attraction");

            br = new BufferedReader(new FileReader(new File("roads.csv")));
            while((line = br.readLine()) != null)
                roadsList.add(new Road(line.split(",")[0],line.split(",")[1],Integer.valueOf(line.split(",")[2])));
            br.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void buildCityList(){
        for (int i = 0; i < roadsList.size(); i++) {
            String location1=roadsList.get(i).getLocation1();
            if(!cityList.contains(location1))
                cityList.add(location1);

            String location2=roadsList.get(i).getLocation2();
            if(!cityList.contains(location2))
                cityList.add(location2);
        }
    }

    public List<Vertex> route(String starting_city, String ending_city, List<String> attractions){
        nodes = new ArrayList<Vertex>();
        edges= new ArrayList<Edge>();

        //Add nodes
        for (int i = 0; i < cityList.size(); i++) {
            Vertex location = new Vertex("NodeID_" + i, cityList.get(i));
            nodes.add(location);
        }

        //Add edges
        for (int i = 0; i < roadsList.size(); i++) {
            String location1=roadsList.get(i).getLocation1();
            String location2=roadsList.get(i).getLocation2();

            addLane(location1+"-"+location2,
                    cityList.indexOf(location1),
                    cityList.indexOf(location2),
                    roadsList.get(i).getDistance());
            addLane(location2+"-"+location1,cityList.indexOf(location2),cityList.indexOf(location1),roadsList.get(i).getDistance());

        }

        Graph graph = new Graph(nodes, edges);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);

        if(attractions.size()==0){
            dijkstra.execute(nodes.get(cityList.indexOf(starting_city)));
            LinkedList<Vertex> path = dijkstra.getPath(nodes.get(cityList.indexOf(ending_city)));
            System.out.println(dijkstra.getShortestDistance(nodes.get(cityList.indexOf(ending_city))));
            return path;
        }

        String source=starting_city;
        List<LinkedList<Vertex>> paths=new ArrayList<>();

        //run this algorithm with attractions.size() times
        Integer times=attractions.size();
        for(int i=0;i<times;i++){
            dijkstra.execute(nodes.get(cityList.indexOf(source)));

            //find the shortest path from source to all remaining attractions
            Integer distance=dijkstra.getShortestDistance(nodes.get(cityList.indexOf(attractions.get(0))));
            LinkedList<Vertex> path=dijkstra.getPath(nodes.get(cityList.indexOf(attractions.get(0))));
            source=attractions.get(0);
            for(int j=0;j<attractions.size();j++){
                Integer distanceTemp=dijkstra.getShortestDistance(nodes.get(cityList.indexOf(attractions.get(j))));
                if(distance>distanceTemp){
                    distance=distanceTemp;
                    path=dijkstra.getPath(nodes.get(cityList.indexOf(attractions.get(j))));
                    source=attractions.get(j);
                }
            }
            totalCost+=distance;
            paths.add(path);
            attractions.remove(source);
        }

        //Add the route from last attraction to ending_city
        dijkstra.execute(nodes.get(cityList.indexOf(source)));
        paths.add(dijkstra.getPath(nodes.get(cityList.indexOf(ending_city))));
        totalCost+=dijkstra.getShortestDistance(nodes.get(cityList.indexOf(ending_city)));

        //combine all paths together in one whole path
        LinkedList<Vertex> finalPath=new LinkedList<>();
        finalPath.add(nodes.get(cityList.indexOf(starting_city)));
        for(int i=0;i<paths.size();i++){
            paths.get(i).remove(0);
            finalPath.addAll(paths.get(i));
        }

        return finalPath;

    }

    private void addLane(String laneId, int sourceLocNo, int destLocNo, int weight) {
        Edge lane = new Edge(laneId,nodes.get(sourceLocNo), nodes.get(destLocNo), weight );
        edges.add(lane);
    }

    private void printPath(List<Vertex> path){
        System.out.println("Here is the best route for your trip: ");
        for(int i=0;i<path.size()-1;i++){
            System.out.println("* "+path.get(i)+" -> "+path.get(i+1));
        }
        System.out.println("Total cost: "+totalCost+ " miles");
    }



    public static void main(String[] args){
        RoadTrip roadTrip=new RoadTrip();
        roadTrip.loadingData();
        roadTrip.buildCityList();

        System.out.println("Name of starting city (or EXIT to quit): ");
        String starting_city=scanner.nextLine();
        //3. Check the user input
        if(starting_city.equalsIgnoreCase("exit")){
            System.exit(0);
        }
        System.out.println("Name of ending city: ");
        String ending_city=scanner.nextLine();
        ArrayList<String> inputCities=new ArrayList<>();
        while(true){
            System.out.println("List an attraction along the way (or ENOUGH to stop listing): ");
            String input=scanner.nextLine();
            if(input.equalsIgnoreCase("enough")){
                break;
            }
            if(roadTrip.attractions.get(input)==null){
                System.out.println("Attraction "+input+" unknown.");
                continue;
            }
            inputCities.add(input);
        }

        ArrayList<String> attractionsCity=new ArrayList<>();
        for(int i=0;i<inputCities.size();i++)
            attractionsCity.add(roadTrip.attractions.get(inputCities.get(i)));

        LinkedList<Vertex> path= (LinkedList<Vertex>) roadTrip.route(starting_city,ending_city,attractionsCity);
        roadTrip.printPath(path);
    }
}
