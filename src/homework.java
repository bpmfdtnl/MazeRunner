import java.io.*;
import java.util.*;

/**
 * @author: billwang
 * @create: 9/2/20
 */
public class homework {
    public static void main(String[] args) {
        //Initialize Initial state and build action table.
        HashMap<Location, ArrayList<Integer>> actions = new HashMap<>();
        try (BufferedReader bi = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
             BufferedWriter bo = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt")))) {
            //First Line
            String searchType = bi.readLine();

            //Second Line
            String[] strNums = bi.readLine().split("\\s");
            int[] dimension = new int[3];
            for (int i = 0; i < strNums.length; i++) dimension[i] = Integer.parseInt(strNums[i]);

            //Third Line
            int[] nums = new int[3];
            strNums = bi.readLine().split("\\s");
            for (int i = 0; i < strNums.length; i++) nums[i] = Integer.parseInt(strNums[i]);
            Location start = new Location(nums[0], nums[1], nums[2]);

            //Fourth Line
            strNums = bi.readLine().split("\\s");
            for (int i = 0; i < strNums.length; i++) nums[i] = Integer.parseInt(strNums[i]);
            Location end = new Location(nums[0], nums[1], nums[2]);

            //Fifth Line
            int N = Integer.parseInt(bi.readLine());

            //Sixth Line
            HashMap<Location, ArrayList<Integer>> map = new HashMap<>();
            for (int i = 0; i < N; i++) {
                strNums = bi.readLine().split("\\s");

                for (int j = 0; j < 3; j++) nums[j] = Integer.parseInt(strNums[j]);
                Location loc = new Location(nums[0], nums[1], nums[2]);
                ArrayList<Integer> action = new ArrayList<>();

                for (int j = 3; j < strNums.length; j++) action.add(Integer.parseInt(strNums[j]));
                map.put(loc, action);
            }
            //Finish reading inputs
            Answer ans;
            switch (searchType){
                case "BFS": ans = BFS(dimension, start, end, map);
                break;
                case "UCS":
                    ans = UCS(dimension, start, end, map);
                break;
                case "A*": ans = AStar(dimension, start, end, map);
                break;
                default: throw new IllegalStateException("Unexpected value: " + searchType);
            }

            if (ans == null) {
                bo.write("FAIL");
            } else {
                bo.write(ans.toString());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Answer AStar(int[] dimension, Location start, Location end, HashMap<Location, ArrayList<Integer>> map) {
        HashSet<Location> visited = new HashSet<>();
        HashMap<Location, State> path = new HashMap<>();
        Queue<Location> queue = new PriorityQueue<>(new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                return Integer.compare(path.get(o1).futureCost, path.get(o2).futureCost);
            }
        });
        path.put(start, new State(0, 0, null));
        queue.add(start);

        while (true){
            if (queue.isEmpty()) return null;
            Location current = queue.poll();
            if (current.equals(end)) {
                return new Answer(current, path);
            }
            if (!map.containsKey(current)) continue;
            for (int i = 0; i < map.get(current).size(); i++) {
                int move = map.get(current).get(i);
                int cost = move > 6 ? 14 : 10;
                Location child = makeMove(current, move);
                int futureCost = hFunction(child, end);
                if (!visited.contains(child) && !queue.contains(child)) {
                    path.put(child, new State(path.get(current).pastCost + cost, path.get(current).pastCost + cost+ futureCost, current));
                    queue.add(child);
                } else if(path.get(current).pastCost + cost < path.get(child).pastCost) {
                    path.put(child, new State(path.get(current).pastCost + cost,path.get(current).pastCost + cost+ futureCost, current));
                }
            }
            visited.add(current);
        }
    }

    private static int hFunction(Location child, Location end) {
        return (int) Math.floor(Math.pow(child.X - end.X, 2) + Math.pow(child.Y - end.Y, 2) + Math.pow(child.Z - end.Z,2)) / 2;
    }

    private static Answer UCS(int[] dimension, Location start, Location end, HashMap<Location, ArrayList<Integer>> map) {
        HashSet<Location> visited = new HashSet<>();
        HashMap<Location, State> path = new HashMap<>();
        Queue<Location> queue = new PriorityQueue<>(new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                return Integer.compare(path.get(o1).pastCost, path.get(o2).pastCost);
            }
        });
        path.put(start, new State(0, null));
        queue.add(start);

        while (true){
            if (queue.isEmpty()) return null;
            Location current = queue.poll();
            if (current.equals(end)) {
                return new Answer(current, path);
            }
            if (!map.containsKey(current)) continue;
            for (int i = 0; i < map.get(current).size(); i++) {
                int move = map.get(current).get(i);
                int cost = move > 6 ? 14 : 10;
                Location child = makeMove(current, move);
                if (!visited.contains(child) && !queue.contains(child)) {
                    path.put(child, new State(path.get(current).pastCost + cost, current));
                    queue.add(child);
                } else if(path.get(current).pastCost + cost < path.get(child).pastCost) {
                    path.put(child, new State(path.get(current).pastCost + cost, current));
                }
            }
            visited.add(current);
        }
    }

    private static Answer BFS(int[] dimension, Location start, Location end, HashMap<Location, ArrayList<Integer>> map) {
        HashSet<Location> visited = new HashSet<>();
        Queue<Location> queue = new LinkedList<>();
        HashMap<Location, State> path = new HashMap<>();
        queue.add(start);
        path.put(start, new State(0, null));

        while (true){
            if (queue.isEmpty()) return null;
            Location current = queue.poll();
            if (current.equals(end)) return new Answer(current, path);
            if (!map.containsKey(current)) continue;
            for (int i = 0; i < map.get(current).size(); i++) {
                Location child = makeMove(current, map.get(current).get(i));
                if (!visited.contains(child) && !queue.contains(child)) {
                    queue.add(child);
                    path.put(child, new State(path.get(current).pastCost + 1, current));
                }
            }
            visited.add(current);
        }
    }




    private static Location makeMove(Location location, Integer integer) {
        //X+ X- Y+ Y- Z+ Z- X+Y+ X+Y- X-Y+ X-Y- X+Z+ X+Z- X-Z+ X-Z- Y+Z+ Y+Z- Y-Z+ Y-Z-
        //1  2  3  4  5  6  7    8    9    10   11   12   13   14   15   16   17   18
        int X = location.X;
        int Y = location.Y;
        int Z = location.Z;
        switch (integer) {
            case 1:
                return new Location(X + 1, Y, Z);
            case 2:
                return new Location(X - 1, Y, Z);
            case 3:
                return new Location(X, Y + 1, Z);
            case 4:
                return new Location(X, Y - 1, Z);
            case 5:
                return new Location(X, Y, Z + 1);
            case 6:
                return new Location(X, Y, Z - 1);
            case 7:
                return new Location(X + 1, Y + 1, Z);
            case 8:
                return new Location(X + 1, Y - 1, Z);
            case 9:
                return new Location(X - 1, Y + 1, Z);
            case 10:
                return new Location(X - 1, Y - 1, Z);
            case 11:
                return new Location(X + 1, Y, Z + 1);
            case 12:
                return new Location(X + 1, Y, Z - 1);
            case 13:
                return new Location(X - 1, Y, Z + 1);
            case 14:
                return new Location(X - 1, Y, Z - 1);
            case 15:
                return new Location(X, Y + 1, Z + 1);
            case 16:
                return new Location(X, Y + 1, Z - 1);
            case 17:
                return new Location(X, Y - 1, Z + 1);
            case 18:
                return new Location(X, Y - 1, Z - 1);
            default:
                return new Location(X, Y, Z);
        }
    }

}

class State {
    int pastCost;
    int futureCost;
    Location parent;

    public State(int pastCost, int futureCost, Location parent) {
        this.pastCost = pastCost;
        this.futureCost = futureCost;
        this.parent = parent;
    }

    public State(int pastCost, Location parent) {
        this.pastCost = pastCost;
        this.parent = parent;
    }
}

class Location {
    int X;
    int Y;
    int Z;

    public Location(int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return X == location.X &&
                Y == location.Y &&
                Z == location.Z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(X, Y, Z);
    }

    @Override
    public String toString() {
        return "Location{" +
                "X=" + X +
                ", Y=" + Y +
                ", Z=" + Z +
                '}';
    }
}

class Answer {
    int totalCost;
    int stepNum = 0;
    ArrayList<ArrayList<Integer>> steps;

    public Answer(Location current, HashMap<Location, State> path) {
        totalCost = path.get(current).pastCost;
        steps = new ArrayList<>();
        do {
            ArrayList<Integer> step = new ArrayList<>();
            State currState = path.get(current);
            step.add(current.X);
            step.add(current.Y);
            step.add(current.Z);
            if (currState.parent == null) step.add(0);
            else step.add(currState.pastCost - path.get(currState.parent).pastCost);
            steps.add(step);
            stepNum++;
            current = currState.parent;
        } while (current != null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(totalCost).append('\n').append(stepNum).append('\n');
        for (int i = steps.size() - 1; i >= 0 ; i--) {
            for (int j: steps.get(i)){
                sb.append(j).append(' ');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append('\n');
        }
        return sb.toString();
    }
}
