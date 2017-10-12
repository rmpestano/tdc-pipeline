package com.github.rmpestano.tdc.cars.bean;

import com.github.adminfaces.persistence.service.CrudService;
import com.github.adminfaces.persistence.service.Service;
import com.github.rmpestano.tdc.cars.model.Car;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.stream.IntStream;

//@Singleton
//@Startup

/**
 * @deprecated
 *
 * Replaced by db migrations
 */
@Stateless
public class InitAppMB implements Serializable {

    @Inject
    @Service
    protected CrudService<Car,Integer> crudService;

    @PostConstruct
    public void init() {
        IntStream.rangeClosed(1, 50)
                .forEach(i -> create(i));
    }


    protected void create(int i) {
        create("model " + i,"name " + i,Double.valueOf(i));
    }

    public void create(String model, String name, Double price) {
        crudService.insert(new Car().model(model).name(name).price(price));
    }

    public void deleteAll() {
        crudService.getEntityManager().createQuery("Delete from Car c").executeUpdate();
    }


}
