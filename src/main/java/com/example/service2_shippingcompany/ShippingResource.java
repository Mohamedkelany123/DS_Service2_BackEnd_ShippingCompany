package com.example.service2_shippingcompany;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
            String URL = baseUrl + "shippingEndPoint/sendNotification/" + message + "/" + customerName + "/" + orderId;
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

    @POST
    @Path("/login/{company_name}/{password}")
    public Response loginShippingCompany(@PathParam("company_name") String company_name, @PathParam("password") String password, @Context HttpServletRequest request) {
        try {
            shippingcompanyEntity foundSellingCompany = entityManager.createQuery("SELECT a FROM shippingcompanyEntity a WHERE a.company_name = :company_name AND a.password = :password", shippingcompanyEntity.class)
                    .setParameter("company_name", company_name)
                    .setParameter("password", password)
                    .getSingleResult();
            if (foundSellingCompany != null) {
                HttpSession session = request.getSession();
                session.setAttribute("company_name", company_name);
                return Response.status(Response.Status.OK).entity("LoggedIn Successfully!").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid username or password!").build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Error!").build();
        } finally {
            entityManager.close();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response.status(Response.Status.OK).entity("LoggedOut Successfully!").build();
    }

    @GET
    @Path("/listCompatiblePendingOrders")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllShippingCompanies(@Context HttpServletRequest request) throws URISyntaxException {
        HttpSession session = request.getSession();
        String company_name = (String) session.getAttribute("company_name");

        TypedQuery<String> queryLocation = entityManager.createQuery("SELECT ca.geographic_coverage FROM shippingcompanyEntity ca WHERE ca.company_name = :username", String.class);
        queryLocation.setParameter("username", company_name);
        String location = queryLocation.getSingleResult();

        String URL = baseUrl + "shippingEndPoint/listCompatiblePendingOrders/" + location;
        try
        {
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(URL)).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("processOrderById/{id}")
    public Response processOrderById(@PathParam("id") int id, @Context HttpServletRequest request){
        HttpSession session = request.getSession();
        String company_name = (String) session.getAttribute("company_name");

        String URL = baseUrl + "shippingEndPoint/processOrderById/" + id + "/" + company_name;
        try
        {
            HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofMinutes(1))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.ofString());
            return Response.status(Response.Status.OK).entity("Order id[" + id + "] Processed successfully!").build();
        } catch (IOException e) {
            System.out.println("Exception 1");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("Exception 2");
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/listProcessingOrders")
    @Produces(MediaType.APPLICATION_JSON)
    public String listProcessingOrders(@Context HttpServletRequest request) throws URISyntaxException {
        HttpSession session = request.getSession();
        String company_name = (String) session.getAttribute("company_name");
        String URL = baseUrl + "shippingEndPoint/listProcessingOrders/" + company_name;
        try
        {
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(URL)).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("setShippedById/{id}")
    public Response setShippedById(@PathParam("id") int id, @Context HttpServletRequest request){
        HttpSession session = request.getSession();
        String company_name = (String) session.getAttribute("company_name");

        String URL = baseUrl + "shippingEndPoint/setShippedById/" + id + "/" + company_name;
        try
        {
            HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .timeout(java.time.Duration.ofMinutes(1))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.ofString());
            return Response.status(Response.Status.OK).entity("Order id[" + id + "] Processed successfully!").build();
        } catch (IOException e) {
            System.out.println("Exception 1");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("Exception 2");
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/listShippedOrders")
    @Produces(MediaType.APPLICATION_JSON)
    public String listShippedOrders(@Context HttpServletRequest request) throws URISyntaxException {
        HttpSession session = request.getSession();
        String company_name = (String) session.getAttribute("company_name");
        String URL = baseUrl + "shippingEndPoint/listShippedOrders/" + company_name;
        try
        {
            HttpClient httpClient = HttpClient.newBuilder().build();
            HttpRequest httpRequest = HttpRequest.newBuilder().uri(new URI(URL)).GET().build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}