package com.uniovi.sdi.reservationmanagement;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected @NonNull SpringApplicationBuilder configure(@NonNull SpringApplicationBuilder application) {
        return application.sources(ReservationManagementApplication.class);
    }

}
