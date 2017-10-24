package com.github.rmpestano.tdc.it;

import com.github.adminfaces.template.exception.AccessDeniedException;
import com.github.rmpestano.tdc.pipeline.infra.security.LogonMB;
import com.github.rmpestano.tdc.pipeline.model.Car;
import com.github.rmpestano.tdc.pipeline.model.Car_;
import com.github.rmpestano.tdc.pipeline.service.CarService;
import com.github.rmpestano.tdc.util.Deployments;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.arquillian.ArquillianCucumber;
import cucumber.runtime.arquillian.api.Features;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(ArquillianCucumber.class)
@Features({"features/search-cars.feature", "features/remove-cars.feature"})
public class CarsBdd {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive war = Deployments.createDeployment("cars-bdd.war");
        MavenResolverSystem resolver = Maven.resolver();
        war.addAsLibraries(resolver.loadPomFromFile("pom.xml").resolve("org.assertj:assertj-core").withTransitivity().asFile());
        System.out.println(war.toString(true));
        return war;
    }

    @Inject
    CarService carService;

    @Inject
    LogonMB logonMB;

    Car carFound;

    int numCarsFound;

    String message;

    @Given("^Database is initialized$")
    public void initDatabase()  {
        logonMB.login("admin");
        carService.removeAll();
        createCar("Ferrari","ferrari spider",2450.8);
        createCar("Mustang","mustang spider",12999.0);
        createCar("Porche","porche avenger",1390.3);
        createCar("Porche274","porche rally",18990.23);
    }

    @Given("^search car with model \"([^\"]*)\"$")
    public void searchCarWithModel(String model) {
        Car carExample = new Car().model(model);
        carFound = (Car) carService.example(carExample, Car_.model).getSingleResult();
        assertThat(carFound).isNotNull()
                .extracting("name")
                .contains("ferrari spider");
    }

    @When("^update model to \"([^\"]*)\"$")
    public void updateModel(String model) {
        carFound.model(model);
        carService.update(carFound);
    }

    @Then("^searching car by model \"([^\"]*)\" must return (\\d+) of records$")
    public void searchingCarByModel(final String model, final int result) {
        Car carExample = new Car().model(model);
        assertThat(result).isEqualTo(carService.count(carService.example(carExample,Car_.model)).intValue());
    }

    @When("^search car with price less than (.+)$")
    public void searchCarWithPrice(final double price) {
        numCarsFound = carService.count(carService.criteria().ltOrEq(Car_.price, price)).intValue();
    }

    @Then("^must return (\\d+) cars")
    public void mustReturnCars(final int result) {
        assertEquals(result, numCarsFound);
    }


    @When("^\"([^\"]*)\" is removed$")
    public void ferrari_is_removed(String model) throws Throwable {
        assertThat(carFound.getModel()).isEqualTo(model);
        try {
            carService.remove(carFound);
        } catch (AccessDeniedException ex) {
            message = ex.getMessage();
        }
    }

    @Then("^there is no more cars with model \"([^\"]*)\"$")
    public void there_is_no_more_cars_with_model(String model) throws Throwable {

        assertThat(carService.count(carService.criteria().eq(Car_.model, model))).isEqualTo(0);
    }

    @Given("^user is logged in as \"([^\"]*)\"$")
    public void user_is_logged_in_as(String user) throws Throwable {
        logonMB.login(user);
        assertThat(logonMB.getCurrentUser()).isEqualTo(user);
    }

    @Then("^error message must be \"([^\"]*)\"$")
    public void error_message_must_be(String msg) throws Throwable {
        assertThat(msg).isEqualTo(message);
    }

    public void createCar(String model, String name, Double price) {
        carService.insert(new Car().model(model).name(name).price(price));
    }


}
