package org.apache.poi.hssf.model;

import org.apache.poi.hssf.usermodel.HSSFTextbox;

/**
 * @author Evgeniy Berlog
 * @date 25.06.12
 */
public class HSSFTestModelHelper {
    public static TextboxShape createTextboxShape(int shapeId, HSSFTextbox textbox){
        return new TextboxShape(textbox, shapeId);
    }
}
