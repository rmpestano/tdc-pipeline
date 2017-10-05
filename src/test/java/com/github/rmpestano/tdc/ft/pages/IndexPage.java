package com.github.rmpestano.tdc.ft.pages;

import org.jboss.arquillian.graphene.GrapheneElement;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.graphene.page.Location;

@Location("index.xhtml")
public class IndexPage {

    @FindByJQuery("H2")
    private GrapheneElement title;


    public GrapheneElement getTitle() {
        return title;
    }
}
