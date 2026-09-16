package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Invoice;
import java.util.List;

/** Persists the invoices of checked-out bookings so they are not lost at checkout time. */
public interface InvoiceHistoryRepository {

    void save(Invoice invoice);

    List<Invoice> loadAll();
}