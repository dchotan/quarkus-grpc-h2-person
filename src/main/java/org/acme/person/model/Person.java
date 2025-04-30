package org.acme.person.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.List;

@Entity
public class Person extends PanacheEntity
{
   @Column(nullable = false)
   public String name;

   public Person() {}

   public Person(String name)
   {
      this.name = name;
   }

   public static List<Person> findByName(String name)
   {
      return list("name", name);
   }
}
