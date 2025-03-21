package com.mci.ticketpilot.data.repository;

import com.mci.ticketpilot.data.entity.Project;
import com.mci.ticketpilot.data.entity.Ticket;
import com.mci.ticketpilot.data.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("select c from Ticket c " +
            "where lower(c.ticketName) like lower(concat('%', :searchTerm, '%')) ")
    List<Ticket> search(@Param("searchTerm") String searchTerm);

    @Query("SELECT c.ticketProject FROM Ticket c WHERE c = :ticket")
    Project findProjectToTicket(Ticket ticket);

    @Query("SELECT c FROM Ticket c WHERE c.ticketUser = :ticketUser")
    List<Ticket> findByAssignee(Users ticketUser);
}