package com.example.service2_shippingcompany;

import jakarta.persistence.*;

@Entity
@Table(name = "shippingcompany")
public class shippingcompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String company_name;

    @Column(name = "password")
    private String password;

    @Column(name = "geographic_coverage")
    private String geographic_coverage;

    public shippingcompanyEntity() {}

    public shippingcompanyEntity(String company_name, String password, String geographic_coverage) {
        this.company_name = company_name;
        this.password = password;
        this.geographic_coverage = geographic_coverage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return company_name;
    }

    public void setCompanyName(String companyName) {
        this.company_name = companyName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGeographicCoverage() {
        return geographic_coverage;
    }

    public void setGeographicCoverage(String geographicCoverage) {
        this.geographic_coverage = geographicCoverage;
    }
}
