package org.acme.person.grpc;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped; // Use ApplicationScoped
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.acme.person.model.Person;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@GrpcService // Register this class as a gRPC service endpoint
//@ApplicationScoped // Services are typically ApplicationScoped
@Singleton
public class PersonGrpcService implements PersonService { // Implements generated service interface

   private static final Logger log = Logger.getLogger(PersonGrpcService.class);

   // --- Create ---
   @Override
   @Transactional // Required for database writes
   public Uni<PersonResponse> createPerson(CreatePersonRequest request) {
      log.infof("Received createPerson request for name: %s", request.getName());
      if (request.getName() == null || request.getName().trim().isEmpty()) {
         // Basic validation - gRPC typically uses status codes for errors
         // For simplicity, we'll return an empty response here, but proper error handling is better
         log.warn("Cannot create person with empty name");
         // You could throw an exception mapped to a gRPC status code
         // throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Name cannot be empty"));
         return Uni.createFrom().item(PersonResponse.newBuilder().build());
      }

      return Uni.createFrom().item(() -> {
               Person newPerson = new Person(request.getName());
               newPerson.persist(); // Persist the new entity
               log.infof("Created person with ID: %d", newPerson.id);
               return newPerson; // Return the persisted entity (with ID)
            })
            .map(persistedPerson -> PersonResponse.newBuilder()
                  .setPerson(toProtoPerson(persistedPerson))
                  .build());
   }

   // --- Read Methods (Unchanged from previous example) ---

   @Override
   @Transactional // Good practice for consistency
   public Uni<PersonResponse> findById(PersonByIdRequest request) {
      log.infof("Received findById request for id: %d", request.getId());
      return Uni.createFrom().item(() -> Person.<Person>findById(request.getId()))
            .map(personEntity -> {
               PersonResponse.Builder responseBuilder = PersonResponse.newBuilder();
               if (personEntity != null) {
                  responseBuilder.setPerson(toProtoPerson(personEntity));
               }
               return responseBuilder.build();
            });
   }

   @Override
   @Transactional
   public Uni<PeopleResponse> findByName(PersonByNameRequest request) {
      log.infof("Received findByName request for name: %s", request.getName());
      return Uni.createFrom().item(() -> Person.findByName(request.getName()))
            .map(personList -> PeopleResponse.newBuilder()
                  .addAllPeople(personList.stream()
                        .map(this::toProtoPerson)
                        .collect(Collectors.toList()))
                  .build());
   }

   @Override
   @Transactional
   public Uni<PeopleResponse> getAll(GetAllPeopleRequest request) {
      log.info("Received getAll request");
      return Uni.createFrom().item(() -> Person.<Person>listAll())
            .map(personList -> PeopleResponse.newBuilder()
                  .addAllPeople(personList.stream()
                        .map(this::toProtoPerson)
                        .collect(Collectors.toList()))
                  .build());
   }

   // --- Update ---
   @Override
   @Transactional // Required for database writes
   public Uni<PersonResponse> updatePerson(UpdatePersonRequest request) {
      log.infof("Received updatePerson request for id: %d, new name: %s", request.getId(), request.getName());
      if (request.getName() == null || request.getName().trim().isEmpty()) {
         log.warn("Cannot update person with empty name");
         // Consider throwing StatusRuntimeException(Status.INVALID_ARGUMENT)
         return Uni.createFrom().item(PersonResponse.newBuilder().build());
      }

      return Uni.createFrom().item(() -> {
               Person existingPerson = Person.findById(request.getId());
               if (existingPerson != null) {
                  existingPerson.name = request.getName();
                  // No explicit persist() needed for updates on managed entities within a transaction
                  log.infof("Updated person with ID: %d", existingPerson.id);
                  return existingPerson;
               } else {
                  log.warnf("Person with ID %d not found for update.", request.getId());
                  return null; // Indicate not found
               }
            })
            .map(updatedPerson -> {
               PersonResponse.Builder responseBuilder = PersonResponse.newBuilder();
               if (updatedPerson != null) {
                  responseBuilder.setPerson(toProtoPerson(updatedPerson));
               }
               // If updatedPerson is null (not found), response will be empty (no person field)
               return responseBuilder.build();
            });
   }

   // --- Delete ---
   @Override
   @Transactional // Required for database writes
   public Uni<DeletePersonResponse> deletePerson(DeletePersonRequest request) {
      log.infof("Received deletePerson request for id: %d", request.getId());
      return Uni.createFrom().item(() -> Person.deleteById(request.getId())) // deleteById returns boolean
            .map(deleted -> {
               if (deleted) {
                  log.infof("Deleted person with ID: %d", request.getId());
               } else {
                  log.warnf("Person with ID %d not found for deletion.", request.getId());
               }
               return DeletePersonResponse.newBuilder().setSuccess(deleted).build();
            });
   }


   // --- Helper method  ---
   private org.acme.person.grpc.Person toProtoPerson(org.acme.person.model.Person entity) {
      if (entity == null) {
         return null;
      }
      return org.acme.person.grpc.Person.newBuilder()
            .setId(entity.id)
            .setName(entity.name)
            .build();
   }
}
