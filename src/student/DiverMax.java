package student;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import game.GetOutState;
import game.Tile;
import game.FindState;
import game.SewerDiver;
import game.Node;
import game.NodeStatus;
import game.Edge;

public class DiverMax extends SewerDiver {


	/** Get to the ring in as few steps as possible. Once you get there, 
	 * you must return from this function in order to pick
	 * it up. If you continue to move after finding the ring rather 
	 * than returning, it will not count.
	 * If you return from this function while not standing on top of the ring, 
	 * it will count as a failure.
	 * 
	 * There is no limit to how many steps you can take, but you will receive
	 * a score bonus multiplier for finding the ring in fewer steps.
	 * 
	 * At every step, you know only your current tile's ID and the ID of all 
	 * open neighbor tiles, as well as the distance to the ring at each of these tiles
	 * (ignoring walls and obstacles). 
	 * 
	 * In order to get information about the current state, use functions
	 * currentLocation(), neighbors(), and distanceToRing() in FindState.
	 * You know you are standing on the ring when distanceToRing() is 0.
	 * 
	 * Use function moveTo(long id) in FindState to move to a neighboring 
	 * tile by its ID. Doing this will change state to reflect your new position.
	 * 
	 * A suggested first implementation that will always find the ring, but likely won't
	 * receive a large bonus multiplier, is a depth-first walk. Some
	 * modification is necessary to make the search better, in general.*/
	@Override public void findRing(FindState state) {
		//TODO : Find the ring and return.
		// DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
		// Instead, write your method elsewhere, with a good specification,
		// and call it from this one.
		HashSet<Long> flag=new HashSet<>();
		dfsWalkNew(state,flag);
		return;
	}

	/** The walker is standing on a Node u (say) given by State s.
    Visit every node reachable along paths of unvisited nodes from 
    node u in order of increasing distance to the Ring.
    End with walker standing on Node u.
    Precondition: u is unvisited. */
	public static boolean dfsWalkNew(FindState now,HashSet<Long> flag) {
		if(now.distanceToRing()==0)
			return true;
		long nowId=now.currentLocation();
		flag.add(nowId);
		List<NodeStatus> nowNeighbors=(List<NodeStatus>)now.neighbors();
		Collections.sort(nowNeighbors);
		for(NodeStatus w: nowNeighbors) {
			long wId=w.getId();
			if (!flag.contains(wId)) {
				now.moveTo(wId);
				if(dfsWalkNew(now,flag)==true)
					return true;
				now.moveTo(nowId);
			}
		}
		return false;
	}


	/** Get out of the sewer system before the steps are all used, trying to collect
	 * as many coins as possible along the way. Your solution must ALWAYS get out
	 * before the steps are all used, and this should be prioritized above
	 * collecting coins.
	 * 
	 * You now have access to the entire underlying graph, which can be accessed
	 * through GetOutState. currentNode() and getExit() will return Node objects
	 * of interest, and getNodes() will return a collection of all nodes on the graph. 
	 * 
	 * You have to get out of the sewer system in the number of steps given by
	 * getStepsRemaining(); for each move along an edge, this number is decremented
	 * by the weight of the edge taken.
	 * 
	 * Use moveTo(n) to move to a node n that is adjacent to the current node.
	 * When n is moved-to, coins on node n are automatically picked up.
	 * 
	 * You must return from this function while standing at the exit. Failing to
	 * do so before steps run out or returning from the wrong node will be
	 * considered a failed run.
	 * 
	 * Initially, there are enough steps to get from the starting point to the
	 * exit using the shortest path, although this will not collect many coins.
	 * For this reason, a good starting solution is to use the shortest path to
	 * the exit. */
	@Override public void getOut(GetOutState state) {
		//TODO: Get out of the sewer system before the steps are used up.
		// DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
		//with a good specification, and call it from this one.

		List<List<Node>> all=new ArrayList<>();
		List<Node> path1=shortest(state);
		all.add(path1);
		List<Node> path2=farthest(state);
		all.add(path2);
		List<Node> path3=orderEverytime(state);
		all.add(path3);
		List<Node> path4=highValueM(state);
		all.add(path4);
		List<Node> path5=highValueMGetNeighbor(state);
		all.add(path5);
		List<Node> path6=highRatioM(state);
		all.add(path6);

		all.sort((p1,p2)->coinGet(p2)-coinGet(p1));
		for(Node w:all.get(0)) {
			state.moveTo(w);
		}
	}
	/**shortest type, make sure we can find a way to get out*/
	public static List<Node> shortest(GetOutState state){
		new Paths();
		List<Node> shortest=Paths.shortestPath(state.currentNode(), state.getExit());
		shortest.remove(0);
		return shortest;
	}

	/**Find the farthest node m as an intermediary node, use shortest way to get the m
	 * and get to the exist in shortest way*/
	public static List<Node> farthest(GetOutState state){
		new Paths();
		HashMap<Node,Integer> startToAll=Paths.shortestPath(state.currentNode());
		HashMap<Node,Integer> finalToAll=Paths.shortestPath(state.getExit());
		int stepL=state.stepsLeft();
		int min=Integer.MAX_VALUE;
		Node media=null;
		Iterator<Entry<Node, Integer>> iter=startToAll.entrySet().iterator();	
		while(iter.hasNext()) {
			Entry<Node,Integer> next=iter.next();
			int l1=next.getValue();
			int l2=finalToAll.get(next.getKey());
			if(l1+l2<=stepL)
				if(stepL-(l1+l2)<min) {
					min=stepL-(l1+l2);
					media=next.getKey();
				}
		}
		List<Node> shortest=Paths.shortestPath(state.currentNode(), media);
		List<Node> shortest2=Paths.shortestPath(media, state.getExit());
		shortest.remove(0);
		shortest2.remove(0);
		for(Node t:shortest2) {
			shortest.add(t);
		}
		return shortest;
	}

	/**Find the most expensive node m as an intermediary node, 
	 * use shortest way to get to the m, and find the second 
	 * expensive one,  third one....until we need to get to the exist*/
	public static List<Node> highValueM(GetOutState state){
		new Paths();
		HashMap<Node,Integer> finalToAll=Paths.shortestPath(state.getExit());
		int stepL=state.stepsLeft();
		List<Node> allNodes=new LinkedList<>();
		for(Node w:state.allNodes()) {
			allNodes.add(w);
		}
		allNodes.sort((n1,n2)->n2.getTile().coins()-n1.getTile().coins());
		Node stand=state.currentNode();
		HashMap<Node,Integer> standToAll=Paths.shortestPath(state.currentNode());
		List<Node> road=new ArrayList<>();
		HashSet<Node> flag=new HashSet<>();
		for(Node w:allNodes) {
			if(!flag.contains(w)) {
				int l1=standToAll.get(w);
				int l2=finalToAll.get(w);
				if(l1+l2<stepL) {
					stepL-=l1;
					List<Node> path=Paths.shortestPath(stand, w);
					flag.addAll(path);
					path.remove(0);
					road.addAll(path);
					stand=w;
					standToAll=Paths.shortestPath(stand);
				}
			}
		}
		List<Node> pathToExit=Paths.shortestPath(stand, state.getExit());
		pathToExit.remove(0);
		road.addAll(pathToExit);
		return road;
	}

	/**Every time when we move to a new node, order its neighbors according to:
	 * coins>unvisited>visited>previous until we have to directly get to the exist*/
	public static List<Node> orderEverytime(GetOutState state){
		new Paths();
		ArrayList<Node> richest=new ArrayList<>();
		HashMap<Node,Integer> finalToAll=Paths.shortestPath(state.getExit());
		HashMap<Node,Integer> cumulative=new HashMap<>();
		HashSet<Node> flagGrab=new HashSet<>();
		int stepLeft=state.stepsLeft();
		Node stand=state.currentNode();
		Node backNode=null;
		while(!stand.equals(state.getExit())) {
			Heap<Node> goldN=new Heap<>();
			ArrayList<Node> unvisitedN=new ArrayList<>();
			Heap<Node> visitedN=new Heap<>();
			for(Node w:stand.getNeighbors()) {
				if(stand.getEdge(w).length+finalToAll.get(w)<=stepLeft) {
					if(w.getTile().coins()>0&&!flagGrab.contains(w))
						goldN.add(w, w.getTile().coins()*(-1));
					else if(!cumulative.containsKey(w)) {
						unvisitedN.add(w);
					}
					else if(!w.equals(backNode)){
						visitedN.add(w, cumulative.get(w));
					}
					if(w.getNeighbors().size()==1) 
						cumulative.put(w, 100);
				}
			}
			Node b=stand;
			if(goldN.size>0) {
				stand=goldN.peek();
				flagGrab.add(stand);
			}
			else if(unvisitedN.size()>0)
				stand=unvisitedN.get(0);
			else if(visitedN.size>0)
				stand=visitedN.peek();
			else
				stand=backNode;
			richest.add(stand);
			backNode=b;
			if(!cumulative.containsKey(stand))
				cumulative.put(stand, 1);
			else
				cumulative.put(stand, cumulative.get(stand)+1);
			stepLeft-=b.getEdge(stand).length;
		}
		return richest;
	}

	/**Find the most expensive node m as an intermediary node, 
	 * use shortest way to get to the m and collect neighbor coins along the way
	 * then get to the second expensive one,  third one....until we have
	 * to get to the exist directly*/
	public static List<Node> highValueMGetNeighbor(GetOutState state){
		new Paths();
		List<Node> allNodes=new LinkedList<>();
		for(Node w:state.allNodes()) {
			allNodes.add(w);
		}
		HashMap<Node,Integer> finalToAll=Paths.shortestPath(state.getExit());
		HashMap<Node,Integer> standToAll=Paths.shortestPath(state.currentNode());
		allNodes.sort((n1,n2)->n2.getTile().coins()-n1.getTile().coins());
		Node stand=state.currentNode();
		int stepL=state.stepsLeft();
		List<Node> road=new ArrayList<>();
		HashSet<Node> flag=new HashSet<>();
		for(Node w:allNodes) {
			if(!flag.contains(w)) {
				standToAll=Paths.shortestPath(stand);
				int l1=standToAll.get(w);
				int l2=finalToAll.get(w);
				if(l1+l2<stepL) {
					List<Node> path=Paths.shortestPath(stand, w);
					stepL-=l1;
					if(path.size()==1)
						break;
					int pathIndex=1;
					while(true) {
						stand=path.get(pathIndex);
						road.add(stand);
						flag.add(stand);
						if(stand.equals(w))
							break;
						for(Node n:stand.getNeighbors()) {
							if(!n.equals(path.get(pathIndex+1))&&!flag.contains(n)) {
								if(n.getTile().coins()>0)
									if(2*n.getEdge(stand).length+l2<stepL) {
										road.add(n);
										road.add(stand);
										flag.add(n);
										stepL-=stand.getEdge(n).length*2;
									}

							}
						}
						pathIndex++;
					}
				}
			}
		}
		List<Node> pathToExit=Paths.shortestPath(stand, state.getExit());
		pathToExit.remove(0);
		road.addAll(pathToExit);
		return road;
	}

	/**Find an intermediary node that from current node to this node,
	 * we can get the highest ratio of the coins we can get to the 
	 * cost of steps of this path. Continue to find such an intermediary 
	 * node until we have to get to the exist directly */
	public static List<Node> highRatioM(GetOutState state){
		new Paths();
		HashMap<Node,Integer> finalToAll=Paths.shortestPath(state.getExit());
		int stepL=state.stepsLeft();
		List<Node> allNodes=new LinkedList<>();
		for(Node w:state.allNodes()) {
			if(w.getTile().coins()>0)
				allNodes.add(w);
		}
		Node stand=state.currentNode();
		HashMap<Node,Integer> standToAll=Paths.shortestPath(state.currentNode());
		List<Node> road=new ArrayList<>();
		HashSet<Node> flag=new HashSet<>();
		boolean stop=false;
		while(!stop) {
			stop=true;
			HashMap<Node,Double> ratio=new HashMap<>();
			HashMap<Node,List<Node>> pathMap=new HashMap<>();
			for(Node w:allNodes) {
				if(!flag.contains(w)) {
					int wCoinSum=0;
					pathMap.put(w, Paths.shortestPath(stand,w));
					for(Node v:pathMap.get(w)) {
						if(!flag.contains(v))
							wCoinSum+=v.getTile().coins();
					}
					if(standToAll.get(w)!=0)
						ratio.put(w,(wCoinSum/(double)standToAll.get(w)));
				}
			}
			List<HashMap.Entry<Node,Double>> ratioList=new ArrayList<>();
			ratioList.addAll(ratio.entrySet());
			ratioList.sort((p1,p2)->p2.getValue().compareTo(p1.getValue()));
			for(Entry<Node, Double> s:ratioList) {
				Node w=s.getKey();
				if(!flag.contains(w)) {
					int l1=standToAll.get(w);
					int l2=finalToAll.get(w);
					if(l1+l2<stepL) {
						stop=false;
						stepL-=l1;
						List<Node> path=pathMap.get(w);
						flag.addAll(path);
						path.remove(0);
						road.addAll(path);
						stand=w;
						standToAll=Paths.shortestPath(stand);
					}
				}
				break;
			}


		}
		List<Node> pathToExit=Paths.shortestPath(stand, state.getExit());
		pathToExit.remove(0);
		road.addAll(pathToExit);
		return road;
	}




	/**Compute how many coins we can get using this path*/
	public static int coinGet(List<Node> path){
		HashSet<Node> flag=new HashSet<>();
		int ans=0;
		for(Node w:path) {
			if(!flag.contains(w))
				ans+=w.getTile().coins();
			flag.add(w);
		}
		return ans;
	}
}
