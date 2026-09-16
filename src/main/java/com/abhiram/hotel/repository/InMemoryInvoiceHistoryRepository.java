package com.abhiram.hotel.repository;

import com.abhiram.hotel.domain.Invoice;
import java.util.ArrayList;
import java.util.List;

/** In-memory {@link InvoiceHistoryRepository} for tests and for a single-session run. */
public final class InMemoryInvoiceHistoryRepository implements InvoiceHistoryRepository {

    private final List<Invoice> invoices = new ArrayList<>();

    @Override
    public void save(Invoice invoice) {
        invoices.add(invoice);
    }

    @Override
    public List<Invoice> loadAll() {
        return List.copyOf(invoices);
    }
}