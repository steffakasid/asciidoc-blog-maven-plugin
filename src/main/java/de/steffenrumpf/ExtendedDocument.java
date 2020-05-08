package de.steffenrumpf;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

import org.asciidoctor.ast.Document;

public class ExtendedDocument implements Comparable<ExtendedDocument> {

    private File file = null;
    private Document document = null;

    public ExtendedDocument(Document document, File file) {
        setFile(file);
        setDocument(document);
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return this.document;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Map<String, Object> getAttributes() {
        return this.document.getAttributes();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ExtendedDocument(getDocument(), getFile());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExtendedDocument) {
            ExtendedDocument objIsInst = (ExtendedDocument) obj;
            if(objIsInst.getDocument().equals(getDocument()) && objIsInst.getFile().equals(getFile())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }

    @Override
    public String toString() {
        return getFile().getAbsolutePath();
    }

    @Override
    public int compareTo(ExtendedDocument o) {
        if( !equals(o)) {
            Map<String, Object> oAttrs = o.getAttributes();
            Map<String, Object> thisAttrs = getAttributes();
            String oDateStr = (String) oAttrs.get("site-date");
            String thisDateStr = (String) thisAttrs.get("site-date");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.GERMAN);
            
            LocalDate oDate = LocalDate.parse(oDateStr, formatter);
            LocalDate thisDate = LocalDate.parse(thisDateStr, formatter);
            
            // we'll invert the value so we get a descending order 
            return thisDate.compareTo(oDate) * -1;
        }
        return 0;
    }
    
}