package com.studynote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.studynote.mapper")
public class StudyNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyNoteApplication.class, args);
    }
}
