//Scott Spencer
//Minimum Spanning Trees - Kruskal's Algorithm

import java.io.*;
import java.util.*;


public class kruskal {
	
	//Edge Record data type
	public static class EdgeRecord {
		//contains 2 vertices, an edge weight, and "next"
		int v1;
		int v2;
		double weight;
		EdgeRecord next;   //MAYBE change NODES to edgerecords, change next to up?
		
		public EdgeRecord(int vx1, int vx2, double w) {
			this.v1 = vx1;
			this.v2 = vx2;
			this.weight = w;
		}
		
		public EdgeRecord(int vx1, int vx2, double w, EdgeRecord e) {
			this.v1 = vx1;
			this.v2 = vx2;
			this.weight = w;
			this.next = e;
		}
	}
	
	//MinHeap data structure (array implementation)
	public static class Heap {
		//array-based implementation
		//insert, deleteMin operations
		//use min heap
		EdgeRecord[] heapArray; //to keep track of array
		int heapSize;
		
		public Heap() {
			heapArray = new EdgeRecord[5000];
			heapSize = 0;
		}
		
		public EdgeRecord deleteMin() {
			EdgeRecord x = heapArray[0];
			heapSize--;
			//replace deleted 0 index with bottom child and move back down heap to sort
			heapArray[0] = heapArray[heapSize];
			downHeap(0);
			return x; 
		}
		
		public void insert(EdgeRecord e) {
			heapArray[heapSize] = e;
			heapSize++;
			//move newest entry up heap to appropriate position/sort array.
			upHeap(heapSize - 1);
		}
		
		//move edge up heap structure recursively for as long as this weight is less than parent
		//weight
		public void upHeap(int position) {
			if (position > 0) {
				//if new edge has a weight less than its current parent's weight...
				if (heapArray[getParentIndex(position)].weight > heapArray[position].weight) {
					//swap new edge with parent
					EdgeRecord temp = heapArray[position];
					heapArray[position] = heapArray[getParentIndex(position)];
					heapArray[getParentIndex(position)] = temp;
					//recursively call upHeap again from the new edge's updated position
					upHeap(getParentIndex(position));
				}
			}
		}
		
		//move edge down heap structure recursively for as long as this weight is less than parent
		//weight
		public void downHeap(int position) {
			int i = 0;
			if (getRChildIndex(position) < heapSize) { //both children exist
				if (heapArray[getRChildIndex(position)].weight <= heapArray[getLChildIndex(position)].weight) {
					i = getRChildIndex(position);
				}
				else {
					i = getLChildIndex(position);
				}
			}
			else if (getLChildIndex(position) < heapSize) { //only the left child exists
				i = getLChildIndex(position);
			}
			//if i = 0 here, node has no children
			if ( i > 0 && heapArray[position].weight > heapArray[i].weight) {
				//swap new edge with appropriate child index "i"
				EdgeRecord temp = heapArray[position];
				heapArray[position] = heapArray[i];
				heapArray[i] = temp;
				//recursively cal downHeap again from the new edge's updated position
				downHeap(i);
			}
		}

		private int getParentIndex(int nodeIndex) {
			return (nodeIndex - 1) / 2;
		}
		
		private int getLChildIndex(int nodeIndex) {
			return 2 * nodeIndex + 1;
		}
 
		private int getRChildIndex(int nodeIndex) {
			return 2 * nodeIndex + 2;
		}
		
		public int size() {
			return heapSize;
		}
	}
	
	//Node subclass for upTree
	public static class Node {
		int key; //just its index in the array "forest"
		int data;
		Node up;
		int count;
		
		public Node(int k, int v) {
			key = k;
			data = v;
			//up will be null
			count = 1;
		}
		
		public Node(int k, int v, Node parent) {
			key = k;
			data = v;
			up = parent;
			count = 1;
		}
			
		public int count() {
			return count;
		}
	}
	
	//Up-Tree data structure (array implementation for accessing individual nodes)
	public static class UpTree {
		//using array to access individual nodes in the "forest,"  each one can still be navigated 
		//using "up" pointers to find relationships
		Node[] keys = new Node[1000];
		int keySize = 0;
		
		//make new set
		public Node makeSet(int v) {
			int k = keySize;
			Node n = new Node(keySize, v);
			keys[keySize] = n;
			keySize++;
			
			return n;
		}
		
		//follow parent pointers until root is reached
		public Node find(int data) {
			int i = 0;
			//loop through array to find appropriate node, then find its parent
			while (keys[i].data != data) {
				i++;
			}
			Node p = keys[i];
			//loop through members of "p's" set to find root member
			while (p.up != null) {
				p = p.up;
			}
			return p;
			//PATH COMPRESSION: after performing find, make all nodes along the path to the root CHILDREN of the root)
		}
		
		public Node pathCompressFind(int data) {
			int i = 0;
			//loop through array to find appropriate node, then find its parent
			while (keys[i].data != data) {
				i++;
			}
			Node p = keys[i];
			//do similar find operations as above but this time use path compression
			Node r = find(p.data);
			Node q = p;
			Node s;
			while (q != r) {
				s = q;
				q = p.up;
				s.up = r;
			}
			return r;
		}
		
		//Add up counts for parent counts, promote appropriate node to parent position
		public Node union(Node s, Node t) {
			if (s.count >= t.count) {
				s.count = s.count + t.count;
				t.up = s;
				return s;
			}
			else {
				t.count = s.count + t.count;
				s.up = t;
				return t;
			}
		}

	}
	
	//New data type vertex for AdjList, which holds references to other adjacent vertices
	public static class Vertex {
		int id;
		Vertex next;
		
		//constructor
		public Vertex(int data) {
			this.id = data;
		}
		
		//returns true if vertex has a "next" vertex it points to
		public boolean hasNext() {
			if (this.next != null) {
				return true;
			}
			return false;
		}
	}
	
	//lists adjacent vertices to the vertex associated with a given index (an array of linked lists)
	public static class AdjList {
		
		//array of vertices, which have a pointers to other adjacent vertices
		Vertex[] adjArray;
		int adjSize;
		
		public AdjList() {
			//only 1000 vertices
			adjArray = new Vertex[1000]; 
			adjSize = 0;
		}
		
		//merge sort a linked list
		public Vertex mergeSort(Vertex h) {
			//base case: head is null
			if (h == null || h.next == null) {
				return h;
			}
			
			//get middle of list
			Vertex middle = getMiddle(h);
			Vertex nextofmiddle = middle.next;
			
			middle.next = null;
			//sort the left and right halves of the list recursively
			//it will split the list continuously and merge, sorting it
			Vertex left = mergeSort(h);
			Vertex right = mergeSort(nextofmiddle);
			Vertex sortedlist = sortedMerge(left, right);
			
			return sortedlist;
		}
		
		//sorted  merge
		public Vertex sortedMerge(Vertex a, Vertex b) {
			Vertex result = null;
			//base cases
			if (a == null) {
				return b;
			}
			if (b == null) {
				return a;
			}
			
			//pick a or b and recurse
			if (a.id < b.id) {
				result = a;
				result.next = sortedMerge(a.next, b);
			}
			else {
				result = b;
				result.next = sortedMerge(a, b.next);
			}
			return result;
		}
		
		//function to get the middle of linked list for sorting 
		public Vertex getMiddle(Vertex h) {
			//base case
			if (h == null) {
				return h;
			}
			//fast and slow pointers
			Vertex fastptr = h.next;
			Vertex slowptr = h;
			//move fastptr by 2 and slowptr by 1 
			//slowptr will end up in the middle of the list
			while (fastptr != null) {
				fastptr = fastptr.next;
				if (fastptr != null) {
					slowptr = slowptr.next;
					fastptr = fastptr.next;
				}
			}
			return slowptr;
		}
		
		//Insert an edge into the adjacency list
		public void insert(EdgeRecord e) {
			//set v1 and v2 in edgerecord to vertex
			Vertex v1 = new Vertex(e.v1);
			Vertex v2 = new Vertex(e.v2);
			
			//check if the current indices for v1/v2 are empty, because they won't be after this
			//operation, and size should reflect that
			if (adjArray[v1.id] == null) {
				adjSize++;
			}
			if (adjArray[v2.id] == null) {
				adjSize++;
			}
			
			//point v1 and v2's next to the current entry at the index of its adjacent vertex
			//but make sure vertices appear in increasing order by calling the sorting function
			//then, set v1 and v2 to the current entry at the index of their adjacent vertex
			
			v2.next = adjArray[v1.id];
			adjArray[v1.id] = v2;
			
			v1.next = adjArray[v2.id];
			adjArray[v2.id] = v1;
		}
		
		//returns size of adjacency array (number of vertices with at least one adjacency)
		public int size() {
			return this.adjSize;
		}
	}
	
	
	//linked list with a record of vertices in the structure
	public static class MST { 
		//number of edges in the MST
		int mstSize;
		//reference to a "head" edge
		EdgeRecord head;
		
		public MST() {
			this.mstSize = 0;
		}
		//inserts an edge into the MST (if it can be inserted)
		public EdgeRecord insert(EdgeRecord e) {
			//if one or both vertices of edge are not in MST, add it.
			//keep record of vertex which was not already in MST
			if (head == null) {
				head = e;
				mstSize++;
				return head;
			}
			EdgeRecord temp = head;
			while (temp.next != null) {
				temp = temp.next;
			}
			temp.next = e;
			mstSize++;
			return head.next;
		}

		//returns the number of edges in the MST
		public int size() {
			return this.mstSize;
		}
	}

	//Class variables for our structures
	static AdjList adjList = new AdjList();  //ordered adjacency list (array implementation)
	static UpTree upTree = new UpTree();  //array implementation of upTree to keep track of change
	static Heap heap = new Heap();  //array implementation of heap structure
	static MST spanTree = new MST();  //Our MST, to be computed by the program
	
	//READ in input which on each line contains the information for an edge.
	//2 integers for the endpoints of the edge, and a real number representing the weight of the edge.
	//assume vertices are numbered 0 to "n,"  than n < 1000, and that the number of edges "m" < 5000
	//The last line of the file will contain -1 to denote the end of input.
	public static void main(String[] args) {
		System.out.println("Please enter the name of the input file: ");
		//use a heap, an adjacency list, an up-tree, and kruskal's algorithm
		Scanner input = new Scanner(System.in);
		String fileName = input.nextLine();
		File inFile = new File(fileName); 
		//change scanner to the file user wants to use
		try {
			input = new Scanner(inFile);
		} catch(FileNotFoundException e) {
			System.out.println("Invalid file.  Please try again");
			System.exit(1);
		}
		
		//FOR ALL lines of input...
		//read line of input for edge "e"
		EdgeRecord e;
		String line = input.nextLine();
		String[] values = line.split(" +");
		
		while (Integer.valueOf(values[0]) != -1) {
			e = new EdgeRecord(Integer.valueOf(values[0]), Integer.valueOf(values[1]), Double.valueOf(values[2]));
			//turn data in "line" into edge record "e"
			adjList.insert(e);
			heap.insert(e); //do heap AND aList take edge records? aList has to be in order...

			line = input.nextLine();
			values = line.split(" +");	
		}
		
		//print heap (line for each edge.  9 chars, first endpoint r-justified field of 4, space,
		//second endpoint r-justified field of 4. make sure first is smaller than second)
		System.out.println("HEAP (MinHeap structure of edges): ");
		for (int i = 0; i < heap.size(); i++) {
			//make sure the smaller vertex of the edge is printed first
			if (heap.heapArray[i].v1 < heap.heapArray[i].v2) {
				System.out.printf("%4d", heap.heapArray[i].v1);
				System.out.print(" ");
				System.out.printf("%4d", heap.heapArray[i].v2);
			}
			else {
				System.out.printf("%4d", heap.heapArray[i].v2);
				System.out.print(" ");
				System.out.printf("%4d", heap.heapArray[i].v1);
			}
			System.out.println();
		}
		
		
		//compute MST using Kruskal's algorithm
		computeMST();
		
		//print MST (line for each edge OF MST.  Printed as described for heap. Edges should appear
		//in lexicographic order, meaning edges of first vertex 0 before those with first vertex 1,
		//and edges with the same first vertex in order according to their second vertices.)
		EdgeRecord temp = spanTree.head;
		System.out.println("Minimum Spanning Tree (lowest total edge-cost for a tree visiting each vertex): ");
		while (temp != null) {
			//make sure the smaller vertex of the edge is printed first
			if (temp.v1 < temp.v2) {
				System.out.printf("%4d ", temp.v1);
				System.out.printf("%4d", temp.v2);
			}
			else {
				System.out.printf("%4d ", temp.v2);
				System.out.printf("%4d", temp.v1);
			}
			System.out.println();
			temp = temp.next;
		}
		
		//print adjacency list (line for each vertex (n+1). For each line "i" we print the vertices
		//adjacent to vertex "i," but not including i.  All vertices r-justified and separated by a
		//space if they appear on the same line) 
		Vertex temp2;
		System.out.println("Adjacency List (listing adjacent vertices to vertex matching row number): ");
		//loop through list (including vertices with no adjacencies)
		for (int m = 0; m < adjList.size(); m++) {
			//using temp, loop through all "next" pointers at index i, and print them to 4 chars
			temp2 = adjList.adjArray[m];
			temp2 = adjList.mergeSort(temp2);
			while (temp2 != null) {
				System.out.printf("%4d ", temp2.id);
				temp2 = temp2.next;
			}
			//skip a line for the next vertex index
			System.out.println();
		}
	}
	//use deleteMin from heap to build the MST
	public static void computeMST() {
		//Select edges in order of increasing "cost" (weight)
		//Accept an edge to expand tree or forest only if it does not cause a cycle
		//Implementation using adjacency list, priority queues and disjoint sets
		EdgeRecord e;
		//array to keep track of vertices in MST
		int[] vertices = new int[1000];
		//number of vertices in the MST thus far
		int vertSize = 0;
		
		//loop through heap
		//heap.size() will change everytime we deleteMin, so we have to use its ORIGINAL value for 
		//our loop bound
		int loop = heap.size(); 
		for (int i = 0; i < loop; i++) {
			//get minimum value from heap
			e = heap.deleteMin();
			//check if edge's vertices are in the array of vertices.  If not, add them and make 
			//them into sets
			if (!inVertices(vertices, vertSize, e.v1)) {
				vertices[vertSize] = e.v1;
				vertSize++;
				upTree.makeSet(e.v1);
			}
			if (!inVertices(vertices, vertSize, e.v2)) {
				vertices[vertSize] = e.v2;
				vertSize++;
				upTree.makeSet(e.v2);
			}
			//check if unique node for root of set containing edge's first vertex is equal to unique node
			//for root of set containing edge's second vertex.
			//if it is not, insert edge into minimum spanning tree and union the two sets 
			//containing the vertices of the edge.
			if (upTree.find(e.v1) != upTree.find(e.v2)) {

				spanTree.insert(e);
				upTree.union(upTree.find(e.v1), upTree.find(e.v2));
			}
		}
	}
	
	//checks if a vertex is already in the MST's vertices
	public static boolean inVertices(int[] vertices, int vertSize, int check) {
		for (int i = 0; i < vertSize; i++) {
			if (vertices[i] == check) {
				return true;
			}
		}
		return false;
	}
}