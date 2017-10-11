/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.rmpestano.tdc.cars.model;



import com.github.adminfaces.persistence.model.BaseEntity;

import javax.persistence.*;

/**
 * @author rmpestano
 */
@Entity
@Table(name = "CAR")
public class Car extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MODEL",nullable = false)
    private String model;

    @Column(name = "NAME",nullable = false)
    private String name;

    @Column(name = "PRICE", nullable = false)
    private Double price;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Car() {
    }

    public Car(Integer id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public Double getPrice() {
        return price;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Car model(String model) {
        this.model = model;
        return this;
    }

    public Car price(Double price) {
        this.price = price;
        return this;
    }

    public Car name(String name) {
        this.name = name;
        return this;
    }

    public boolean hasModel() {
        return model != null && !"".equals(model.trim());
    }

    public boolean hasName() {
        return name != null && !"".equals(name.trim());
    }
}
