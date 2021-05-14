package com.pranab.playwithgraphs.weightedgraphs.directed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pranab.playwithgraphs.Edge;
import com.pranab.playwithgraphs.datastructure.LinkedList;
import com.pranab.playwithgraphs.datastructure.MinPriorityHeap;
import com.pranab.playwithgraphs.datastructure.Queue;
import com.pranab.playwithgraphs.datastructure.Stack;
import com.pranab.playwithgraphs.datastructure.implementation.ArrayMinPriorityHeap;
import com.pranab.playwithgraphs.datastructure.implementation.DynamicList;
import com.pranab.playwithgraphs.weightedgraphs.Weight;
import com.pranab.playwithgraphs.weightedgraphs.WeightedEdge;

public class AdjacencyListDirectedWeightedGraph<V, K, W extends Weight> implements DirectedWeightedGraph<V, K, W> {

	Map<K, DirectedWeightedNode<V, K, W>> storage = new HashMap<>();

	@Override
	public void createNode(K key, V value) {
		DirectedWeightedNode<V, K, W> node = new DirectedWeightedNode<>(value);
		storage.put(key, node);
	}

	@Override
	public V removeNode(K key) {
		DirectedWeightedNode<V, K, W> node = storage.remove(key);
		return node.getValue();
	}

	@Override
	public void updateNode(K key, V value) {
		DirectedWeightedNode<V, K, W> node = storage.get(key);
		node.setValue(value);
		storage.put(key, node);
	}

	@Override
	public V getValue(K key) {
		if (storage.containsKey(key)) {
			return storage.get(key).getValue();
		} else {
			return null;
		}
	}

	@Override
	public void createEdge(K sourceNodeKey, K targetNodeKey, W edgeWeight) {
		if (edgeWeight == null) {
			throw new UnsupportedOperationException("Weight can't be null");
		}
		if ((!storage.containsKey(targetNodeKey)) || (!storage.containsKey(sourceNodeKey))) {
			throw new UnsupportedOperationException("Key not found");
		}
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(sourceNodeKey);
		LinkedList<WeightedEdge<K, W>> edgeList = sourceNode.getOutGoingEdges();
		WeightedEdge<K, W> edge = new WeightedEdge<>(targetNodeKey, edgeWeight);
		edgeList.addLast(edge);

		DirectedWeightedNode<V, K, W> inSourceNode = storage.get(targetNodeKey);
		LinkedList<WeightedEdge<K, W>> inEdgeList = inSourceNode.getInComingEdges();
		WeightedEdge<K, W> inEdge = new WeightedEdge<>(sourceNodeKey, edgeWeight);
		inEdgeList.addLast(inEdge);
	}

	@Override
	public void createEdges(K sourceNodeKey, Map<K, W> targetNodeKeyWeightMap) {
		for (Map.Entry<K, W> entry : targetNodeKeyWeightMap.entrySet()) {
			createEdge(sourceNodeKey, entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void updateEdgeWeight(K sourceNodeKey, K targetNodeKey, W edgeWeight) {
		if (edgeWeight == null) {
			throw new UnsupportedOperationException("Weight can't be null");
		}
		if ((!storage.containsKey(targetNodeKey)) || (!storage.containsKey(sourceNodeKey))) {
			throw new UnsupportedOperationException("Key not found");
		}
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(sourceNodeKey);
		LinkedList<WeightedEdge<K, W>> edgeList = sourceNode.getOutGoingEdges();
		edgeList.functionalIterate(v -> {
			if (v.equals(targetNodeKey)) {
				v.setWeights(edgeWeight);
			}
		});

		DirectedWeightedNode<V, K, W> inSourceNode = storage.get(targetNodeKey);
		LinkedList<WeightedEdge<K, W>> inEdgeList = inSourceNode.getInComingEdges();
		inEdgeList.functionalIterate(v -> {
			if (v.equals(sourceNodeKey)) {
				v.setWeights(edgeWeight);
			}
		});
	}

	@Override
	public List<K> getAllEdgeKeys(K sourceNodeKey) {
		List<K> keyList = new ArrayList<>();
		LinkedList<WeightedEdge<K, W>> edgeList = storage.get(sourceNodeKey).getOutGoingEdges();
		for (WeightedEdge<K, W> edge : edgeList) {
			keyList.add(edge.getKeyPointingNode());
		}
		return keyList;
	}

	@Override
	public void removeEdge(K sourceNodeKey, K targetNodeKey) {
		if ((!storage.containsKey(targetNodeKey)) || (!storage.containsKey(sourceNodeKey))) {
			throw new UnsupportedOperationException("Key not found");
		}
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(sourceNodeKey);
		LinkedList<WeightedEdge<K, W>> edgeList = sourceNode.getOutGoingEdges();
		edgeList.removeElement(new WeightedEdge<>(targetNodeKey));
		
		DirectedWeightedNode<V, K, W> inSourceNode = storage.get(targetNodeKey);
		LinkedList<WeightedEdge<K, W>> inEdgeList = inSourceNode.getOutGoingEdges();
		inEdgeList.removeElement(new WeightedEdge<>(sourceNodeKey));
	}

	@Override
	public void removeEdges(K sourceNodeKey, List<K> targetNodeKeys) {
		for (K key : targetNodeKeys) {
			removeEdge(sourceNodeKey, key);
		}
	}

	@Override
	public void removeAllEdges(K sourceNodeKey) {
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(sourceNodeKey);
		for(WeightedEdge<K, W> edge:sourceNode.getOutGoingEdges()) {
			removeEdge(sourceNodeKey,edge.getKeyPointingNode());
		}
	}

	@Override
	public void resetAllNodes() {
		for (DirectedWeightedNode<V, K, W> node : storage.values()) {
			node.setTraversed(false);
			node.setLevel(0);
			node.setPrevPointer(null);
			node.setScore(-1);
		}
	}

	@Override
	public int size() {
		return storage.size();
	}

	@Override
	public List<V> searchLevel(K startingKeyPoint, int searchLevel, boolean includeBeforeLevel) {
		int level = 0;
		List<V> outputList = new ArrayList<>();
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(startingKeyPoint);
		sourceNode.setTraversed(true);
		sourceNode.setLevel(0);
		Queue<DirectedWeightedNode<V, K, W>> queue = new DynamicList<>();
		queue.enqueue(sourceNode);
		while ((!queue.isEmpty()) && level <= searchLevel) {
			DirectedWeightedNode<V, K, W> baseNode = queue.dequeue();
			level = (baseNode.getLevel() + 1);
			for (Edge<K> edge : baseNode.getOutGoingEdges()) {
				DirectedWeightedNode<V, K, W> extractedNode = storage.get(edge.getKeyPointingNode());
				if (!extractedNode.isTraversed()) {
					extractedNode.setTraversed(true);
					extractedNode.setLevel(level);
					queue.enqueue(extractedNode);
					if (includeBeforeLevel && level <= searchLevel) {
						outputList.add(extractedNode.getValue());
					} else {
						if (level == searchLevel) {
							outputList.add(extractedNode.getValue());
						}
					}
				}
			}
		}
		resetAllNodes();
		return outputList;
	}

	@Override
	public java.util.Queue<K> searchSortestPath(K startingKeyPoint, K targetKeyPoint) {
		MinPriorityHeap<Integer, K> minHeap = new ArrayMinPriorityHeap<>();
		DirectedWeightedNode<V, K, W> node = storage.get(startingKeyPoint);
		node.setScore(0);
		minHeap.insert(0, startingKeyPoint);
		while (minHeap.size() != 0) {
			K key = minHeap.extractMin();
			DirectedWeightedNode<V, K, W> minNode = storage.get(key);
			minNode.setTraversed(true);
			int nodeWeight = minNode.getScore();
			for (WeightedEdge<K, W> edge : minNode.getOutGoingEdges()) {
				DirectedWeightedNode<V, K, W> toBeProcessed = storage.get(edge.getKeyPointingNode());
				if (!toBeProcessed.isTraversed()) {
					int score = nodeWeight + edge.getWeights().getWeight();
					if (toBeProcessed.getScore() == -1) {
						toBeProcessed.setPrevPointer(key);
						toBeProcessed.setScore(score);
						minHeap.insert(score, edge.getKeyPointingNode());
					} else if (score < toBeProcessed.getScore()) {
						int indexKey = toBeProcessed.getScore();
						toBeProcessed.setPrevPointer(key);
						toBeProcessed.setScore(score);
						minHeap.increasePriority(minHeap.getIndex(indexKey), score);
					}
				}
			}
		}
		java.util.Queue<K> output=new java.util.LinkedList<>();
		DirectedWeightedNode<V, K, W> targetNode =storage.get(targetKeyPoint);
		output.add(targetKeyPoint);
		while(targetNode.getPrevPointer()!=null) {
			output.add(targetNode.getPrevPointer());
			targetNode=storage.get(targetNode.getPrevPointer());
		}
		resetAllNodes();
		return output;
	}

	@Override
	public boolean checkCycle() {
		Stack<DirectedWeightedNode<V, K, W>> stack = new DynamicList<>();
		boolean result = false;
		for (K key : storage.keySet()) {
			result = checkCycle(key, stack);
			if (result) {
				break;
			}
		}
		resetAllNodes();
		return result;
	}

	private boolean checkCycle(K startingKeyPoint, Stack<DirectedWeightedNode<V, K, W>> stack) {
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(startingKeyPoint);
		if (sourceNode.isTraversed()) {
			return true;
		}
		sourceNode.setTraversed(true);
		stack.push(sourceNode);
		LinkedList<WeightedEdge<K, W>> edgeList = sourceNode.getOutGoingEdges();
		boolean result = false;
		for (Edge<K> edge : edgeList) {
			result = checkCycle(edge.getKeyPointingNode(), stack);
			if (result) {
				return result;
			}
		}
		stack.pop().setTraversed(false);
		return result;
	}

	@Override
	public List<K> getTopologicalOrdered() {
		if (checkCycle()) {
			throw new UnsupportedOperationException("The graph contains a cycle");
		}
		List<K> list = new ArrayList<>();
		for (K key : storage.keySet()) {
			doDepthFirstSearch(key, list);
		}
		resetAllNodes();
		Collections.reverse(list);
		return list;
	}

	private void doDepthFirstSearch(K lookUpKey, List<K> list) {
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(lookUpKey);
		if (sourceNode.isTraversed()) {
			return;
		}
		sourceNode.setTraversed(true);
		LinkedList<WeightedEdge<K, W>> edgeList = sourceNode.getOutGoingEdges();
		for (Edge<K> edge : edgeList) {
			doDepthFirstSearch(edge.getKeyPointingNode(), list);
		}
		list.add(lookUpKey);
	}

	@Override
	public List<List<K>> getStrongConnectedComponent() {
		List<List<K>> connectedComponentLists = new ArrayList<>();
		Stack<K> stack = new DynamicList<>();
		for (K key : storage.keySet()) {
			doDepthFirstSearchOnTransposeeGraph(key, stack);
		}
		resetAllNodes();
		while (!stack.isEmpty()) {
			List<K> connectedComponentList = new ArrayList<>();
			doDepthFirstSearch(stack.pop(), connectedComponentList);
			if (!connectedComponentList.isEmpty()) {
				connectedComponentLists.add(connectedComponentList);
			}
		}
		resetAllNodes();
		return connectedComponentLists;
	}

	private void doDepthFirstSearchOnTransposeeGraph(K lookUpKey, Stack<K> stack) {
		DirectedWeightedNode<V, K, W> sourceNode = storage.get(lookUpKey);
		if (sourceNode.isTraversed()) {
			return;
		}
		sourceNode.setTraversed(true);
		LinkedList<WeightedEdge<K, W>> edgeList = sourceNode.getInComingEdges();
		for (Edge<K> edge : edgeList) {
			doDepthFirstSearchOnTransposeeGraph(edge.getKeyPointingNode(), stack);
		}
		stack.push(lookUpKey);
	}

}
