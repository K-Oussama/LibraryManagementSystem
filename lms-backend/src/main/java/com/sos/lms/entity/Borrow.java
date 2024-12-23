package com.sos.lms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity @EntityListeners(AuditingEntityListener.class)
@Table(name = "Borrow")
public class Borrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer borrowId;
    Integer bookId;
    Integer userId;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    Date issueDate;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    Date returnDate;

    //@JsonSerialize(using=JsonDataSerializer.class)
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
    @Temporal(TemporalType.TIMESTAMP)
    Date dueDate;

}
