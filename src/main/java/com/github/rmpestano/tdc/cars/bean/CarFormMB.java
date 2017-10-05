/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.rmpestano.tdc.cars.bean;

import com.github.adminfaces.persistence.bean.BeanService;
import com.github.adminfaces.persistence.bean.CrudMB;
import com.github.rmpestano.tdc.cars.model.Car;
import com.github.rmpestano.tdc.cars.service.CarService;
import org.omnifaces.cdi.ViewScoped;
import org.omnifaces.util.Faces;

import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author rmpestano
 */
@Named
@ViewScoped
@BeanService(CarService.class)//use annotation instead of setter
public class CarFormMB extends CrudMB<Car> implements Serializable {


    public void afterRemove() {
        try {
            Faces.redirect("car-list.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String getRemoveMessage() {
        return "Car " + entity.getModel()
                + " removed successfully";
    }

    @Override
    public String getCreateMessage() {
        return "Car " + entity.getModel() + " created successfully";
    }

    @Override
    public String getUpdateMessage() {
        return "Car " + entity.getModel() + " updated successfully";
    }

}
