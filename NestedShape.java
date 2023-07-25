import java.awt.Color;
import java.util.*;
import java.awt.Graphics;

class NestedShape extends RectangleShape{
    private ArrayList<Shape> innerShapes = new ArrayList<Shape>();
    public NestedShape(){
        super();
        createInnerShape(0,0,(this.width/2), (this.height/2), this.color, this.borderColor, PathType.BOUNCING, ShapeType.RECTANGLE);
    }
    public NestedShape(int x, int y, int width, int height, int panelWidth, int panelHeight, Color fillColor, Color borderColor, PathType path){
        super(x, y, width, height, panelWidth, panelHeight, fillColor, borderColor, path);
        createInnerShape(0,0,(this.width/2), (this.height/2), this.color, this.borderColor, PathType.BOUNCING, ShapeType.RECTANGLE);
    }
    public NestedShape(int width, int height){
        super(Color.black, Color.black, PathType.BOUNCING);
        this.width = width;
        this.height = height;
    }
    public Shape createInnerShape(int x, int y, int w, int h, Color c, Color bc, PathType pt, ShapeType st){
        Shape innerShape = null;
        switch(st){
            case RECTANGLE:{
                innerShape = new RectangleShape(x, y, w, h, this.width, this.height, c, bc, pt);
                break;
            }
            case OVAL:{
                innerShape = new OvalShape(x, y, w, h, this.width, this.height, c, bc, pt);
                break;
            }
            case NESTED:{
                innerShape = new NestedShape(x, y, w, h, this.width, this.height, c, bc, pt);
                break;
            }
            
        }
        this.innerShapes.add(innerShape);
        innerShape.setParent(this);
        return innerShape;
    }
    public Shape createInnerShape(PathType pt, ShapeType st){
        Shape innerShape = createInnerShape(0,0,(this.width/2), (this.height/2), this.color, this.borderColor, pt, st);
        return innerShape;
        
    }
    public Shape getInnerShapeAt(int index){
        return this.innerShapes.get(index);
    }
    public int getSize(){
    	 return this.innerShapes.size();
    }
    
    public void draw(Graphics g){
        g.setColor(Color.black);
        g.drawRect(this.x, this.y, this.width, this.height);
        g.translate(x, y);
        for (Shape s: innerShapes) {
            s.draw(g);
            s.drawHandles(g);
            s.drawString(g);
        }
        g.translate(-x, -y);
    }
    
    @Override
    public void move(){
        this.path.move();
         for(int i = 0; i < this.getSize(); i++){
            Shape shapeToMove = this.innerShapes.get(i);
            shapeToMove.move();
        }
    }
    public int indexOf(Shape s){
        return innerShapes.indexOf(s);
    }
    public void addInnerShape(Shape s){
        innerShapes.add(s);
        s.setParent(this); 
    }
    public void removeInnerShape(Shape s){
        innerShapes.remove(s);
        s.setParent(null);
    }
    public void removeInnerShapeAt(int index){
        Shape shapeRemoved = (Shape) innerShapes.remove(index);
        shapeRemoved.setParent(null);
    }
    public ArrayList<Shape> getAllInnerShapes(){
        return this.innerShapes;
    }
}