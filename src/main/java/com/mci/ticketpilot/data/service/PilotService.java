package com.mci.ticketpilot.data.service;

import com.mci.ticketpilot.data.entity.Project;
import com.mci.ticketpilot.data.entity.Ticket;
import com.mci.ticketpilot.data.entity.Users;
import com.mci.ticketpilot.data.repository.UserRepository;
import com.mci.ticketpilot.data.repository.ProjectRepository;
import com.mci.ticketpilot.data.repository.TicketRepository;
import com.mci.ticketpilot.security.SecurityService;
import com.mci.ticketpilot.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import static org.apache.poi.ss.usermodel.TableStyleType.headerRow;

@Service
public class PilotService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;

    public PilotService(UserRepository userRepository,
                        ProjectRepository projectRepository,
                        TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.ticketRepository = ticketRepository;
    }

    ////////////////////////////////////////////////////////////////////
    // Users
    ////////////////////////////////////////////////////////////////////
    public List<Users> findAllUsers(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return userRepository.findAll();
        } else {
            return userRepository.search(stringFilter);
        }
    }

    public InputStream exportToExcel(String filterText) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Tickets");

        List<Ticket> tickets = findAllTickets(filterText);

        int rownum = 0;
        XSSFRow headerRow = sheet.createRow(rownum++);
        headerRow.createCell(0).setCellValue("Ticket Name");
        headerRow.createCell(1).setCellValue("Ticket Priority");
        headerRow.createCell(2).setCellValue("Ticket Status");
        headerRow.createCell(3).setCellValue("Project Name");
        headerRow.createCell(4).setCellValue("Person in Charge");

        // Set the width for the columns
        int numColumns = headerRow.getPhysicalNumberOfCells();
        for (int i = 0; i < numColumns; i++) {
            sheet.setColumnWidth(i, 5000);
        }

        for (Ticket ticket : tickets) {
            XSSFRow row = sheet.createRow(rownum++);
            row.createCell(0).setCellValue(ticket.getTicketName());
            row.createCell(1).setCellValue(ticket.getTicketPriority().ordinal());
            row.createCell(2).setCellValue(ticket.getTicketStatus().ordinal());
            row.createCell(3).setCellValue(ticket.getProject().getProjectName());
            row.createCell(4).setCellValue(ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            workbook.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }




    public List<Users> findAllUsers() { return userRepository.findAll(); }
    public long countUsers() {
        return userRepository.count();
    }
    public void deleteUser(Users user) {
        userRepository.delete(user);
    }


    public void saveUser(Users user) {
        if (user == null) {
            System.err.println("Contact is null.");
            return;
        }
        userRepository.saveAndFlush(user);
    }

    ////////////////////////////////////////////////////////////////////
    // Projects
    ////////////////////////////////////////////////////////////////////
    public List<Project> findAllProjects(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return projectRepository.findAll();
        } else {
            return projectRepository.search(stringFilter);
        }
    }

    public List<Project> findAllProjects(){ return projectRepository.findAll(); }
    public long countProjects() { return projectRepository.count(); }
    public void deleteProject(Project project) {
        projectRepository.delete(project);
    }

    public void saveProject(Project project) {
        if (project == null) {
            System.err.println("Project is null.");
            return;
        }
        projectRepository.saveAndFlush(project);
    }

    ////////////////////////////////////////////////////////////////////
    // Tickets
    ////////////////////////////////////////////////////////////////////
    public List<Ticket> findAllTickets(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return ticketRepository.findAll();
        } else {
            return ticketRepository.search(stringFilter);
        }
    }

    public List<Ticket> findAllTickets(){
        return ticketRepository.findAll();
    }
    public long countTickets() { return ticketRepository.count(); }

    public void deleteTicket(Ticket ticket) {
        ticketRepository.delete(ticket);
    }

    public void saveTicket(Ticket ticket) {
        if (ticket == null) {
            System.err.println("Ticket is null.");
            return;
        }
        logger.info("Saving ticket to DB: " + ticket);
        ticketRepository.saveAndFlush(ticket);
    }

    public Project findProjectToTicket(Ticket ticket) {
        return ticketRepository.findProjectToTicket(ticket);
    }


    ////////////////////////////////////////////////////////////////////
    // Data Retrieving Services
    ////////////////////////////////////////////////////////////////////
    public List<Ticket> getUserTickets(){
        Users currentUser = SecurityUtils.getLoggedInUser();
        //logger.info("Current user: " + currentUser);
        if (currentUser != null) {
            return ticketRepository.findByAssignee(currentUser);
        }
        return Collections.emptyList();
    }

    public List<Project> getUserProjects(){
        Users currentUser = SecurityUtils.getLoggedInUser();
        //logger.info("Current user: " + currentUser);
        if (currentUser != null) {
            return projectRepository.findByUser(currentUser);
        }
        return Collections.emptyList();
    }

    public boolean isCurrentUserAssignee(Ticket ticket){
        Users currentUser = SecurityUtils.getLoggedInUser();
        return currentUser != null && currentUser.equals(ticket.getUser());
    }

    public boolean isCurrentUserManager(Project project){
        Users currentUser = SecurityUtils.getLoggedInUser();
        return currentUser != null && currentUser.equals(project.getManager());
    }

}