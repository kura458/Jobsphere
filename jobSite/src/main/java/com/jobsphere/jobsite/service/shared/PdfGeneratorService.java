package com.jobsphere.jobsite.service.shared;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PdfGeneratorService {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14,
            new Color(0, 102, 204));
    private static final Font LABEL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

    public byte[] generateCV(String templateName, Map<String, Object> data) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Header - Name and Title
            addHeader(document, data);

            // Personal Information
            addPersonalInformation(document, data);

            // Professional Summary / About
            addSection(document, data, "summary", "Professional Summary");
            addSection(document, data, "about", "About Me");

            // Experience
            addListSection(document, data, "experience", "Work Experience",
                    List.of("job_title", "company", "duration", "description"));

            // Education
            addListSection(document, data, "education", "Education", List.of("degree", "institution", "year"));

            // Skills
            addSkillsSection(document, data);

            // Projects
            addListSection(document, data, "projects", "Projects",
                    List.of("project_name", "description", "project_url"));

            // Certifications
            addListSection(document, data, "certifications", "Certifications", List.of("name", "issuer", "year"));

            document.close();
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addHeader(Document document, Map<String, Object> data) throws DocumentException {
        Map<String, Object> header = (Map<String, Object>) data.get("header");
        String name = "RESUME";
        if (header != null && header.get("title") != null) {
            name = header.get("title").toString();
        } else if (data.containsKey("personal_information")) {
            Map<String, Object> personal = (Map<String, Object>) data.get("personal_information");
            if (personal.get("full_name") != null) {
                name = personal.get("full_name").toString();
            }
        }

        Paragraph title = new Paragraph(name, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));
        document.add(new LineSeparator(1, 100, new Color(0, 102, 204), Element.ALIGN_CENTER, -2));
        document.add(new Paragraph(" "));
    }

    private void addPersonalInformation(Document document, Map<String, Object> data) throws DocumentException {
        if (!data.containsKey("personal_information"))
            return;
        Map<String, Object> info = (Map<String, Object>) data.get("personal_information");

        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);

        if (info.get("email") != null)
            p.add(new Phrase("Email: " + info.get("email") + " | ", NORMAL_FONT));
        if (info.get("phone") != null)
            p.add(new Phrase("Phone: " + info.get("phone") + " | ", NORMAL_FONT));
        if (info.get("address") != null)
            p.add(new Phrase("Location: " + info.get("address"), NORMAL_FONT));

        document.add(p);
        document.add(new Paragraph(" "));
    }

    private void addSection(Document document, Map<String, Object> data, String key, String title)
            throws DocumentException {
        if (!data.containsKey(key))
            return;
        Object content = data.get(key);
        if (content == null || content.toString().isEmpty())
            return;

        document.add(new Paragraph(title, SECTION_FONT));
        document.add(new Paragraph(content.toString(), NORMAL_FONT));
        document.add(new Paragraph(" "));
    }

    private void addListSection(Document document, Map<String, Object> data, String key, String title,
            List<String> fields) throws DocumentException {
        if (!data.containsKey(key))
            return;
        Object listObj = data.get(key);
        if (!(listObj instanceof List))
            return;
        List<Map<String, Object>> list = (List<Map<String, Object>>) listObj;
        if (list.isEmpty())
            return;

        document.add(new Paragraph(title, SECTION_FONT));
        document.add(new Paragraph(" "));

        for (Map<String, Object> item : list) {
            for (String field : fields) {
                if (item.get(field) != null && !item.get(field).toString().isEmpty()) {
                    Paragraph p = new Paragraph();
                    String label = field.replace("_", " ");
                    label = label.substring(0, 1).toUpperCase() + label.substring(1);

                    p.add(new Phrase(label + ": ", BOLD_FONT));
                    p.add(new Phrase(item.get(field).toString(), NORMAL_FONT));
                    document.add(p);
                }
            }
            document.add(new Paragraph(" "));
        }
    }

    private void addSkillsSection(Document document, Map<String, Object> data) throws DocumentException {
        if (!data.containsKey("skills"))
            return;
        Map<String, Object> skills = (Map<String, Object>) data.get("skills");

        document.add(new Paragraph("Skills", SECTION_FONT));
        document.add(new Paragraph(" "));

        for (Map.Entry<String, Object> entry : skills.entrySet()) {
            if (entry.getValue() instanceof List) {
                Paragraph p = new Paragraph();
                String label = entry.getKey().replace("_", " ");
                label = label.substring(0, 1).toUpperCase() + label.substring(1);

                p.add(new Phrase(label + ": ", BOLD_FONT));
                p.add(new Phrase(String.join(", ", (List<String>) entry.getValue()), NORMAL_FONT));
                document.add(p);
            }
        }
        document.add(new Paragraph(" "));
    }
}
