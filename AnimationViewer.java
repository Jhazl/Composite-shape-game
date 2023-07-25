
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListDataListener;
import java.lang.reflect.Field;

class AnimationViewer extends JComponent implements Runnable {
	private Thread animationThread = null; // the thread for animation
	private static int DELAY = 120; // the current animation speed
	protected NestedShape root;
	private ShapeType currentShapeType = Shape.DEFAULT_SHAPETYPE; // the current shape type,
	private PathType currentPathType = Shape.DEFAULT_PATHTYPE; // the current path type
	private Color currentColor = Shape.DEFAULT_COLOR; // the current fill colour of a shape
	private Color currentBorderColor = Shape.DEFAULT_BORDER_COLOR;
	private int currentPanelWidth = Shape.DEFAULT_PANEL_WIDTH, currentPanelHeight = Shape.DEFAULT_PANEL_HEIGHT,currentWidth = Shape.DEFAULT_WIDTH, currentHeight = Shape.DEFAULT_HEIGHT;
	private String currentLabel = Shape.DEFAULT_LABEL;
	protected MyModel model;
	public AnimationViewer() {
		start();
		root = new NestedShape(Shape.DEFAULT_PANEL_WIDTH, Shape.DEFAULT_PANEL_HEIGHT);
		addMouseListener(new MyMouseAdapter());
		model = new MyModel();
	}

	public void setCurrentLabel(String text) {
		currentLabel = text;
		for (Shape currentShape : root.getAllInnerShapes())
			if (currentShape.isSelected())
				currentShape.setLabel(currentLabel);
	}
	public void setCurrentColor(Color bc) {
	    currentColor = bc;
	    for (Shape currentShape: root.getAllInnerShapes())
	      if ( currentShape.isSelected())
	        currentShape.setColor(currentColor);
	  }
	public void setCurrentBorderColor(Color bc) {
	    currentBorderColor = bc;
	    for (Shape currentShape: root.getAllInnerShapes())
	      if ( currentShape.isSelected())
	        currentShape.setBorderColor(currentBorderColor);
	 }
	public void setCurrentHeight(int h) {
	    currentHeight = h;
	    for (Shape currentShape: root.getAllInnerShapes())
	      if ( currentShape.isSelected())
	        currentShape.setHeight(currentHeight);
	 }
	public void setCurrentWidth(int w) {
	    currentWidth = w;
	    for (Shape currentShape: root.getAllInnerShapes())
	      if ( currentShape.isSelected())
	        currentShape.setWidth(currentWidth);
	 }
	
	public void resetMarginSize() {
		currentPanelWidth = getWidth();
		currentPanelHeight = getHeight();
		for (Shape currentShape : root.getAllInnerShapes())
			currentShape.resetPanelSize(currentPanelWidth, currentPanelHeight);
	}
	
	
	class MyMouseAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			boolean found = false;
			for (Shape currentShape : root.getAllInnerShapes())
				if (currentShape.contains(e.getPoint())) { // if the mousepoint is within a shape, then set the shape to
					currentShape.setSelected(!currentShape.isSelected());
					//modify here to update model?
					found = true;
				}
			if (!found){
				Shape newChild = root.createInnerShape(e.getX(), e.getY(), currentWidth, currentHeight, currentColor, currentBorderColor,  currentPathType, currentShapeType);
				model.insertNodeInto(newChild, root);
			}
		}
	}
	
	class MyModel extends AbstractListModel<Shape> implements TreeModel{
    private ArrayList<Shape> selectedShapes;
    private ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    public MyModel(){
        selectedShapes = root.getAllInnerShapes();
    }
    public int getSize(){
        return this.selectedShapes.size();
    }
    public Shape getElementAt(int index){
        if (index >= this.getSize() || index < 0){
            return null;
        }else{
            return this.selectedShapes.get(index);
        }
    }
    public void reload(NestedShape selected){
        selectedShapes = selected.getAllInnerShapes();
        this.fireContentsChanged(this, 0, selected.getSize());
    }
    public NestedShape getRoot(){
        return root;
    }
    public boolean isLeaf(Object node){
        return (!(node instanceof NestedShape)  && (node instanceof Shape));
    }
    protected boolean isRoot(Shape selectedNode){
        return (selectedNode == root);
    }
    public Shape getChild(Object parent, int index){
        if(!(parent instanceof NestedShape)){
            return null;
        }else if (index >= ((NestedShape) parent).getSize() || index < 0){
            return null;
        }else{
            return ((NestedShape) parent).getInnerShapeAt(index);
        }
    }
    public int getChildCount(Object parent){
        if(!(parent instanceof NestedShape)){
            return 0;
        }
        else{
            return ((NestedShape) parent).getSize();
        }
    }
    public int getIndexOfChild(Object parent, Object child){
         if(!(parent instanceof NestedShape)){
            return -1;
        }
        else{
            return ((NestedShape) parent).indexOf(((Shape) child));
        }
    }
    public void addTreeModelListener(final TreeModelListener tml){
        treeModelListeners.add(tml);
    }
    public void removeTreeModelListener(final TreeModelListener tml){
        treeModelListeners.remove(tml);
    }
    public void valueForPathChanged(TreePath path, Object newValue){}
    protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children){
        System.out.printf("Called fireTreeNodesInserted: path=%s, childIndices=%s, children=%s\n", Arrays.toString(path), Arrays.toString(childIndices), Arrays.toString(children));
        final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
        for (final TreeModelListener tml : treeModelListeners){
            tml.treeNodesInserted(event);
        }
    }
	
    public void insertNodeInto(Shape newChild, NestedShape parent){
        Shape[] childArray = new Shape[]{newChild};
        int[] childIndices = new int[]{this.getIndexOfChild(parent, newChild)};
        this.fireTreeNodesInserted(this, parent.getPath(), childIndices, childArray);
		this.fireIntervalAdded(this, this.getSize() - 1, this.getSize() - 1);
	    
    }
	
    public void addShapeNode(NestedShape selectedNode){
        Shape newChild = null;
        if(selectedNode.equals(root)){
            newChild = root.createInnerShape(0,  0, currentWidth, currentHeight, currentColor, currentBorderColor,  currentPathType, currentShapeType);
        }else{
            newChild = selectedNode.createInnerShape(currentPathType, currentShapeType);
        }
        this.insertNodeInto(newChild, selectedNode);
    }
    protected void fireTreeNodesRemoved(Object source, Object[] path,int[] childIndices,Object[] children) {
    System.out.printf("Called fireTreeNodesRemoved: path=%s, childIndices=%s, children=%s\n", Arrays.toString(path), Arrays.toString(childIndices), Arrays.toString(children));
    final TreeModelEvent event = new TreeModelEvent(source, path, childIndices, children);
    for (final TreeModelListener tml : treeModelListeners)
      tml.treeNodesRemoved(event);
    }
    public void removeNodeFromParent(Shape selectedNode){
        NestedShape parentNode = ((NestedShape) selectedNode.getParent());
        int index = parentNode.indexOf(selectedNode);
        parentNode.removeInnerShape(selectedNode);
        int[] childIndices = new int[]{index};
        Shape[] children = new Shape[]{selectedNode};
		this.fireIntervalRemoved(this, index, index);
		//this.fireIntervalRemoved(this, this.selectedShapes.indexOf(selectedNode), this.selectedShapes.indexOf(selectedNode));
        fireTreeNodesRemoved(this, parentNode.getPath(), childIndices, children);
		}
	}

	public final void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Shape currentShape : root.getAllInnerShapes()) {
			currentShape.move();
			currentShape.draw(g);
			currentShape.drawHandles(g);
			currentShape.drawString(g);
		}
	}
	

	public String getCurrentLabel() {return currentLabel;}
	public int getCurrentHeight() { return currentHeight; }
	public int getCurrentWidth() { return currentWidth; }
	public Color getCurrentColor() { return currentColor; }
	public Color getCurrentBorderColor() { return currentBorderColor; }
	public void setCurrentShapeType(ShapeType value) {currentShapeType = value;}
	public void setCurrentPathType(PathType value) {currentPathType = value;}
	public ShapeType getCurrentShapeType() {return currentShapeType;}
	public PathType getCurrentPathType() {return currentPathType;}
	public void update(Graphics g) {
		paint(g);
	}
	public void start() {
		animationThread = new Thread(this);
		animationThread.start();
	}
	public void stop() {
		if (animationThread != null) {
			animationThread = null;
		}
	}
	public void run() {
		Thread myThread = Thread.currentThread();
		while (animationThread == myThread) {
			repaint();
			pause(DELAY);
		}
	}
	private void pause(int milliseconds) {
		try {
			Thread.sleep((long) milliseconds);
		} catch (InterruptedException ie) {}
	}
}
