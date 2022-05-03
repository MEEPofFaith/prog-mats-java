package progressed.ui;

import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.struct.*;

public class ShiftedStack extends WidgetGroup{
    private float prefWidth, prefHeight, minWidth, minHeight;
    private boolean sizeInvalid = true;
    protected float stackX, stackY;

    public ShiftedStack(){
        setTransform(false);
        setWidth(150);
        setHeight(150);
        this.touchable = Touchable.childrenOnly;
    }

    public ShiftedStack(Element... actors){
        this();
        for(Element actor : actors)
            addChild(actor);
    }

    @Override
    public void invalidate(){
        super.invalidate();
        sizeInvalid = true;
    }

    private void computeSize(){
        sizeInvalid = false;
        prefWidth = 0;
        prefHeight = 0;
        minWidth = 0;
        minHeight = 0;
        SnapshotSeq<Element> children = getChildren();
        for(int i = 0, n = children.size; i < n; i++){
            Element child = children.get(i);
            if(child != null){
                prefWidth = Math.max(prefWidth, (child).getPrefWidth());
                prefHeight = Math.max(prefHeight, (child).getPrefHeight());
                minWidth = Math.max(minWidth, (child).getMinWidth());
                minHeight = Math.max(minHeight, (child).getMinHeight());
            }else{
                prefWidth = Math.max(prefWidth, child.getWidth());
                prefHeight = Math.max(prefHeight, child.getHeight());
                minWidth = Math.max(minWidth, child.getWidth());
                minHeight = Math.max(minHeight, child.getHeight());
            }
        }
    }

    public void add(Element actor){
        addChild(actor);
    }

    @Override
    public void layout(){
        if(sizeInvalid) computeSize();
        Seq<Element> children = getChildren();
        for(int i = 0, n = children.size; i < n; i++){
            Element child = children.get(i);
            float x = i == 0 ? 0 : (Scl.scl(stackX) + (getWidth() - child.getWidth()) / 2f),
                y = i == 0 ? 0 : (Scl.scl(stackY) + (getHeight() - child.getHeight()) / 2f);
            child.setPosition(x, y);
            if(child != null) child.validate();
        }
    }

    public void setStackPos(float x, float y){
        stackX = x;
        stackY = y;
    }

    @Override
    public float getPrefWidth(){
        if(sizeInvalid) computeSize();
        return prefWidth;
    }

    @Override
    public float getPrefHeight(){
        if(sizeInvalid) computeSize();
        return prefHeight;
    }

    @Override
    public float getMinWidth(){
        if(sizeInvalid) computeSize();
        return minWidth;
    }

    @Override
    public float getMinHeight(){
        if(sizeInvalid) computeSize();
        return minHeight;
    }

}
