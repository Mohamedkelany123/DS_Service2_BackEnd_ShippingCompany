package com.example.service2_shippingcompany;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Path("/shipping-resource")
public class ShippingResource {
    private String baseUrl = "http://localhost:8080/Service1-1.0-SNAPSHOT/api/";

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("mysql");
    private final EntityManager entityManager = emf.createEntityManager();


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/sendNotification/{message}/{customerName}/{orderId}")
    public Response sendNotification(@PathParam("message") String message, @PathParam("customerName") String customerName, @PathParam("orderId") Long orderId) throws IOException, InterruptedException {
        try {
            String URL = baseUrl + "notifications/sendNotification/" + message + "/" + customerName + "/" + orderId;
            HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofMinutes(1))
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build(), HttpResponse.BodyHandlers.ofString());

            return Response.status(Response.Status.CREATED).build();
        }catch(Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to send notification").build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/createShippingCompany/{companyName}/{password}/{geographicCoverage}")
    public void createShippingCompany(@PathParam("companyName") String companyName,@PathParam("password") String password,@PathParam("geographicCoverage") String geographicCoverage) {

        shippingcompanyEntity company = new shippingcompanyEntity(companyName, password, geographicCoverage);

        EntityManager entityManager = null;

        try {
            entityManager = emf.createEntityManager();
            entityManager.getTransaction().begin();
            entityManager.persist(company);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager != null && entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @GET
    @Path("/listShippingCompanies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<shippingcompanyEntity> listShippingCompanies() {
        TypedQuery<shippingcompanyEntity> query = entityManager.createQuery("SELECT c FROM shippingcompanyEntity c", shippingcompanyEntity.class);
        return query.getResultList();
    }

}