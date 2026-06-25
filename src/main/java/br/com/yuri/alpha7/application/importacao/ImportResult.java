package br.com.yuri.alpha7.application.importacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImportResult {

    private int totalProcessed;
    private int totalSaved;
    private final List<String> errors = new ArrayList<>();

    public void incrementSuccess() {
        totalProcessed++;
        totalSaved++;
    }

    public void addError(String message) {
        totalProcessed++;
        errors.add(message);
    }

    public int getTotalProcessed() { return totalProcessed; }
    public int getTotalSaved()     { return totalSaved; }
    public List<String> getErrors() { return Collections.unmodifiableList(errors); }
    public boolean hasErrors()     { return !errors.isEmpty(); }
}
