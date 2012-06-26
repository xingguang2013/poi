/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.*;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.ObjRecord;

/**
 * An abstract shape.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public abstract class HSSFShape {
    public static final int LINEWIDTH_ONE_PT = 12700;
    public static final int LINEWIDTH_DEFAULT = 9525;
    public static final int LINESTYLE__COLOR_DEFAULT = 0x08000040;
    public static final int FILL__FILLCOLOR_DEFAULT = 0x08000009;
    public static final boolean NO_FILL_DEFAULT = true;

    public static final int LINESTYLE_SOLID = 0;              // Solid (continuous) pen
    public static final int LINESTYLE_DASHSYS = 1;            // PS_DASH system   dash style
    public static final int LINESTYLE_DOTSYS = 2;             // PS_DOT system   dash style
    public static final int LINESTYLE_DASHDOTSYS = 3;         // PS_DASHDOT system dash style
    public static final int LINESTYLE_DASHDOTDOTSYS = 4;      // PS_DASHDOTDOT system dash style
    public static final int LINESTYLE_DOTGEL = 5;             // square dot style
    public static final int LINESTYLE_DASHGEL = 6;            // dash style
    public static final int LINESTYLE_LONGDASHGEL = 7;        // long dash style
    public static final int LINESTYLE_DASHDOTGEL = 8;         // dash short dash
    public static final int LINESTYLE_LONGDASHDOTGEL = 9;     // long dash short dash
    public static final int LINESTYLE_LONGDASHDOTDOTGEL = 10; // long dash short dash short dash
    public static final int LINESTYLE_NONE = -1;

    public static final int LINESTYLE_DEFAULT = LINESTYLE_NONE;

    // TODO - make all these fields private
    HSSFShape parent;
    HSSFAnchor anchor;
    HSSFPatriarch _patriarch;

    protected EscherContainerRecord _escherContainer;
    protected ObjRecord _objRecord;
    protected final EscherOptRecord _optRecord;

    public HSSFShape(EscherContainerRecord spContainer, ObjRecord objRecord) {
        this._escherContainer = spContainer;
        this._objRecord = objRecord;
        this._optRecord = spContainer.getChildById(EscherOptRecord.RECORD_ID);
        this.anchor = HSSFAnchor.createAnchorFromEscher(spContainer);
    }

    /**
     * Create a new shape with the specified parent and anchor.
     */
    public HSSFShape(HSSFShape parent, HSSFAnchor anchor) {
        this.parent = parent;
        this.anchor = anchor;
        this._escherContainer = new EscherContainerRecord();
        _optRecord = new EscherOptRecord();
        _optRecord.setRecordId( EscherOptRecord.RECORD_ID );
        _optRecord.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEDASHING, LINESTYLE_SOLID));
        _optRecord.addEscherProperty(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEWIDTH, LINEWIDTH_DEFAULT));
        _optRecord.addEscherProperty(new EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, FILL__FILLCOLOR_DEFAULT));
        _optRecord.addEscherProperty(new EscherRGBProperty(EscherProperties.LINESTYLE__COLOR, LINESTYLE__COLOR_DEFAULT));
        _optRecord.addEscherProperty(new EscherBoolProperty(EscherProperties.FILL__NOFILLHITTEST, 0x0));
    }

    protected abstract EscherContainerRecord createSpContainer();

    protected abstract ObjRecord createObjRecord();

    void setShapeId(int shapeId){
        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setShapeId(shapeId);
        CommonObjectDataSubRecord cod = (CommonObjectDataSubRecord) _objRecord.getSubRecords().get(0);
        cod.setObjectId((short) (shapeId-1024));
    }
    
    int getShapeId(){
        return ((EscherSpRecord)_escherContainer.getChildById(EscherSpRecord.RECORD_ID)).getShapeId();
    }

    void afterInsert(HSSFPatriarch patriarch){
    }

    public EscherContainerRecord getEscherContainer() {
        return _escherContainer;
    }

    public ObjRecord getObjRecord() {
        return _objRecord;
    }

    /**
     * Gets the parent shape.
     */
    public HSSFShape getParent() {
        return parent;
    }

    /**
     * @return the anchor that is used by this shape.
     */
    public HSSFAnchor getAnchor() {
        return anchor;
    }

    /**
     * Sets a particular anchor.  A top-level shape must have an anchor of
     * HSSFClientAnchor.  A child anchor must have an anchor of HSSFChildAnchor
     *
     * @param anchor the anchor to use.
     * @throws IllegalArgumentException when the wrong anchor is used for
     *                                  this particular shape.
     * @see HSSFChildAnchor
     * @see HSSFClientAnchor
     */
    public void setAnchor(HSSFAnchor anchor) {
        int i = 0;
        int recordId = -1;
        if (parent == null) {
            if (anchor instanceof HSSFChildAnchor)
                throw new IllegalArgumentException("Must use client anchors for shapes directly attached to sheet.");
            EscherClientAnchorRecord anch = _escherContainer.getChildById(EscherClientAnchorRecord.RECORD_ID);
            if (null != anch) {
                for (i=0; i< _escherContainer.getChildRecords().size(); i++){
                    if (_escherContainer.getChild(i).getRecordId() == EscherClientAnchorRecord.RECORD_ID){
                        if (i != _escherContainer.getChildRecords().size() -1){
                            recordId = _escherContainer.getChild(i+1).getRecordId();
                        }
                    }
                }
                _escherContainer.removeChildRecord(anch);
            }
        } else {
            if (anchor instanceof HSSFClientAnchor)
                throw new IllegalArgumentException("Must use child anchors for shapes attached to groups.");
            EscherChildAnchorRecord anch = _escherContainer.getChildById(EscherChildAnchorRecord.RECORD_ID);
            if (null != anch) {
                for (i=0; i< _escherContainer.getChildRecords().size(); i++){
                    if (_escherContainer.getChild(i).getRecordId() == EscherChildAnchorRecord.RECORD_ID){
                        if (i != _escherContainer.getChildRecords().size() -1){
                            recordId = _escherContainer.getChild(i+1).getRecordId();
                        }
                    }
                }
                _escherContainer.removeChildRecord(anch);
            }
        }
        if (-1 == recordId){
            _escherContainer.addChildRecord(anchor.getEscherAnchor());
        } else {
            _escherContainer.addChildBefore(anchor.getEscherAnchor(), recordId);
        }
        this.anchor = anchor;
    }

    /**
     * The color applied to the lines of this shape.
     */
    public int getLineStyleColor() {
        EscherRGBProperty rgbProperty = _optRecord.lookup(EscherProperties.LINESTYLE__COLOR);
        return rgbProperty == null ? LINESTYLE__COLOR_DEFAULT : rgbProperty.getRgbColor();
    }

    /**
     * The color applied to the lines of this shape.
     */
    public void setLineStyleColor(int lineStyleColor) {
        setPropertyValue(new EscherRGBProperty(EscherProperties.LINESTYLE__COLOR, lineStyleColor));
    }

    /**
     * The color applied to the lines of this shape.
     */
    public void setLineStyleColor(int red, int green, int blue) {
        int lineStyleColor = ((blue) << 16) | ((green) << 8) | red;
        setPropertyValue(new EscherRGBProperty(EscherProperties.LINESTYLE__COLOR, lineStyleColor));
    }

    /**
     * The color used to fill this shape.
     */
    public int getFillColor() {
        EscherRGBProperty rgbProperty = _optRecord.lookup(EscherProperties.FILL__FILLCOLOR);
        return rgbProperty == null ? FILL__FILLCOLOR_DEFAULT : rgbProperty.getRgbColor();
    }

    /**
     * The color used to fill this shape.
     */
    public void setFillColor(int fillColor) {
        setPropertyValue(new EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, fillColor));
    }

    /**
     * The color used to fill this shape.
     */
    public void setFillColor(int red, int green, int blue) {
        int fillColor = ((blue) << 16) | ((green) << 8) | red;
        setPropertyValue(new EscherRGBProperty(EscherProperties.FILL__FILLCOLOR, fillColor));
    }

    /**
     * @return returns with width of the line in EMUs.  12700 = 1 pt.
     */
    public int getLineWidth() {
        EscherSimpleProperty property = _optRecord.lookup(EscherProperties.LINESTYLE__LINEWIDTH);
        return property == null ? LINEWIDTH_DEFAULT: property.getPropertyValue();
    }

    /**
     * Sets the width of the line.  12700 = 1 pt.
     *
     * @param lineWidth width in EMU's.  12700EMU's = 1 pt
     * @see HSSFShape#LINEWIDTH_ONE_PT
     */
    public void setLineWidth(int lineWidth) {
        setPropertyValue(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEWIDTH, lineWidth));
    }

    /**
     * @return One of the constants in LINESTYLE_*
     */
    public int getLineStyle() {
        EscherSimpleProperty property = _optRecord.lookup(EscherProperties.LINESTYLE__LINEDASHING);
        if (null == property){
            return LINESTYLE_DEFAULT;
        }
        return property.getPropertyValue();
    }

    /**
     * Sets the line style.
     *
     * @param lineStyle One of the constants in LINESTYLE_*
     */
    public void setLineStyle(int lineStyle) {
        setPropertyValue(new EscherSimpleProperty(EscherProperties.LINESTYLE__LINEDASHING, lineStyle));
    }

    /**
     * @return <code>true</code> if this shape is not filled with a color.
     */
    public boolean isNoFill() {
        EscherBoolProperty property = _optRecord.lookup(EscherProperties.FILL__NOFILLHITTEST);
        return property == null ? NO_FILL_DEFAULT : property.isTrue();
    }

    /**
     * Sets whether this shape is filled or transparent.
     */
    public void setNoFill(boolean noFill) {
        setPropertyValue(new EscherBoolProperty(EscherProperties.FILL__NOFILLHITTEST, noFill ? 1 : 0));
    }

    protected void setPropertyValue(EscherProperty property){
        if (null == _optRecord.lookup(property.getId())){
            _optRecord.addEscherProperty(property);
        } else {
            int i=0;
            for (EscherProperty prop: _optRecord.getEscherProperties()){
                if (prop.getId() == property.getId()){
                    _optRecord.getEscherProperties().remove(i);
                    break;
                }
                i++;
            }
            _optRecord.addEscherProperty(property);
        }
    }

    /**
     * Count of all children and their children's children.
     */
    public int countOfAllChildren() {
        return 1;
    }
}
