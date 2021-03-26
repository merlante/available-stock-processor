package org.acme;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.beans.Product;
import org.acme.services.AvailableStockService;

@Path("/stock-available")
public class StockResource {

    @Inject
    AvailableStockService availableStockService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Product,Integer> getAllProductQuantities() {
        return availableStockService.getAllAvailableStock();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("product")
    public Integer getQuantity(Product product) {
        return availableStockService.getAvailableStock(product);
    }
}