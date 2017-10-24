package com.github.rmpestano.tdc.unit;

import com.github.adminfaces.template.exception.BusinessException;
import com.github.rmpestano.tdc.pipeline.model.Car;
import com.github.rmpestano.tdc.pipeline.service.CarService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(JUnit4.class)
public class CarServiceTest {

    @Spy
    CarService carService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        doReturn(true).when(carService).isUniqueName(any(Car.class));
    }


    @Test
    public void shouldNotInsertCarWithoutName() {
        Car car = new Car().model("model").price(1.0);
        try {
            carService.insert(car);
        } catch (BusinessException e) {
            assertThat(e).isNotNull();
            assertThat(e.getExceptionList()).hasSize(1);
            assertThat(e.getExceptionList().get(0)).hasMessage("Car name cannot be empty");
        }
    }

    @Test
    public void shouldNotInsertCarWithoutModel() {
        Car car = new Car().name("name").price(1.0);
        try {
            carService.insert(car);
        } catch (BusinessException e) {
            assertThat(e).isNotNull();
            assertThat(e.getExceptionList()).hasSize(1);
            assertThat(e.getExceptionList().get(0)).hasMessage("Car model cannot be empty");
        }
    }

    @Test
    public void shouldNotInsertCarWithoutNameAndModel() {
        Car car = new Car().price(1.0);
        try {
            carService.insert(car);
        } catch (BusinessException e) {
            assertThat(e).isNotNull();
            assertThat(e.getExceptionList()).hasSize(2);
            assertThat(e.getExceptionList().get(0)).hasMessage("Car model cannot be empty");
            assertThat(e.getExceptionList().get(1)).hasMessage("Car name cannot be empty");
        }
    }

    @Test
    public void shouldNotInsertCarWithDuplicateName() {
        Car car = new Car().name("Car").model("model").price(1.0);
        doReturn(false).when(carService).isUniqueName(car);
        try {
            carService.insert(car);
        } catch (BusinessException e) {
            assertThat(e).isNotNull();
            assertThat(e.getExceptionList()).hasSize(1);
            assertThat(e.getExceptionList().get(0)).hasMessage("Car name must be unique");
        }
    }

    @Test
    public void shouldNotGetTotalPriceByModelWithoutModel() {
        Car car = new Car().name("Car").price(1.0);
        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> carService.getTotalPriceByModel(car))
                .withMessage("Provide car model to get the total price.");
    }


}
