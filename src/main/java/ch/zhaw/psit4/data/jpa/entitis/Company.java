package ch.zhaw.psit4.data.jpa.entitis;

/**
 * Table for companys
 * Created by beni on 20.03.17.
 */

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Company implements Serializable{

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Company(){

    }

    public Company(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
