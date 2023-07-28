package com.leyunone.servicecenter.api.bean;


import lombok.*;

import java.io.Serializable;

/**
 * LeYunone 
 * 
 * 牢房单元格数据 封装两侧
 * @param <Cell>
 * @param <Mate>
 */
@Getter
@Setter
@ToString
public class ResponseCell<Cell,Mate> implements Serializable {

    private Cell cellData;

    private Mate mateDate;
    
    private ResponseCell(){}
    
    public ResponseCell(Cell cellData,Mate mateDate){
        this.cellData = cellData;
        this.mateDate = mateDate;
    }

    public static <Cell,Mate> ResponseCell<Cell,Mate> build(Cell cellData, Mate mateDate) {
        return new ResponseCell<>(cellData, mateDate);
    }
}
