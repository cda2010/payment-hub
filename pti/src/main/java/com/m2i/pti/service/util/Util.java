package com.m2i.pti.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Util {
    private static final String CSV_SEPARATOR = ";";
    /**
     * Calcul et retourne une copie du nombre minimal entre deux BigDecimal
     *
     * @param decimal1
     * @param decimal2
     * @return Le plus petit entre decimal1 et decimal2
     */
    public static BigDecimal min(BigDecimal decimal1, BigDecimal decimal2) {
        BigDecimal d1 = decimal1, d2 = decimal2;
        if (d1 == null && d2 == null) return null;
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.compareTo(d2) <= 0 ? d1 : d2;
    }

    /**
     * Calcul et retourne une copie du nombre maximal entre deux BigDecimal
     *
     * @param decimal1
     * @param decimal2
     * @return Le plus grand entre decimal1 et decimal2
     */
    public static BigDecimal max(BigDecimal decimal1, BigDecimal decimal2) {
        if (decimal1 == null && decimal2 == null) return null;
        if (decimal1 == null) return decimal2;
        if (decimal2 == null) return decimal1;
        return decimal1.compareTo(decimal2) >= 0 ? decimal1 : decimal2;
    }

    public static boolean isNullOrFalse(Boolean bool) {
        return bool == null || !bool;
    }

    public static boolean isTrue(Boolean bool) {
        return (bool != null) && bool;
    }

    public static boolean estPositif(BigDecimal bigDecimal) {
        return bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean estPositifStrict(BigDecimal bigDecimal) {
        return bigDecimal != null && bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isBiggerOrEqualThan(BigDecimal nombre1, BigDecimal nombre2) {
        if (Objects.equals(nombre1, nombre2)) return true;
        if (nombre1 == null) return false;
        if (nombre2 == null) return true;
        return nombre1.compareTo(nombre2) >= 0;
    }

    public static boolean isBiggerThan(BigDecimal nombre1, BigDecimal nombre2) {
        if (Objects.equals(nombre1, nombre2)) return false;
        if (nombre1 == null) return false;
        if (nombre2 == null) return true;
        return nombre1.compareTo(nombre2) > 0;
    }

    /**
     * Retourne la valeur du bigdécimal ou ZERO si celui est null
     *
     * @param montantInitial
     * @return
     */
    public static BigDecimal getZeroIfNull(BigDecimal montantInitial) {
        return montantInitial == null ? BigDecimal.ZERO : montantInitial;
    }

    public static Integer getZeroIfNull(Integer montantInitial) {
        return montantInitial == null ? 0 : montantInitial;
    }

    public static boolean estNullOuZero(BigDecimal montant) {
        return montant == null || BigDecimal.ZERO.compareTo(montant) == 0;
    }

    public static BigDecimal pourcentage(BigDecimal montant, BigDecimal valeur) {
        return montant.multiply(valeur).divide(BigDecimal.valueOf(100));
    }

    public static <T> T convert(Object body, Class<T> resultClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object result = null;

        try {
            String jsonInString = mapper.writeValueAsString(body);
            result = mapper.readValue(jsonInString, resultClass);
        } catch (IOException var6) {
            ;
        }
        return (T) result;
    }

    public static String getMonthForInt(int m) {
        String month = "invalid";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (m >= 0 && m <= 11) {
            month = months[m];
        }
        return month;
    }

    /**
     * use BufferedWriter to write a String to a new file:
     *
     * @throws IOException
     */
    public static void whenWriteStringUsingBufferedWritter_thenCorrect(String fileName, String content)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();
    }

    public static void clear(String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write("");
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readLineByLineAndWrite(String filePath, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));

        List<String> datas = new ArrayList<>();
        datas.add(content);
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> {
                if (!s.equals(content) && s.length() > 0)
                    datas.add(s);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        clear(filePath);
        for (String d : datas) {
            writer.newLine();
            writer.append(d);
        }
        writer.close();
    }

    public static String formatDate(ZonedDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime);
    }
  

    public static String format(ZonedDateTime date, String format) {
        if (Objects.nonNull(date)) {
            return DateTimeFormatter.ofPattern(format).format(date);
        } else {
            return "";
        }
    }

    public static String format(Instant date, String format) {
        if (Objects.nonNull(date)) {
            return DateTimeFormatter.ofPattern(format).format(date.atZone(ZoneId.systemDefault()));
        } else {
            return "";
        }
    }

    public static String formatToddMMYYYY(LocalDate date) {
        if (Objects.nonNull(date))
            return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
        return "";
    }

    public static <T extends String> T marshall(Object classObject, Class<T> resultClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(classObject.getClass());
        Marshaller marchaller = context.createMarshaller();
        marchaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        marchaller.marshal(classObject, sw);

        return (T) (String) sw.toString();
        //return String.format("<?xml version=\"1.0\"?>");
    }


    public static <T extends Object> T unmarshall(String body, Class<T> resultClass) throws JAXBException {

        String strEntry = body.trim();
        StringReader reader = new StringReader(strEntry.replaceAll("<?xml version=\"1.0\"?>", ""));
        JAXBContext context = JAXBContext.newInstance(resultClass);
        Unmarshaller unmarchaller = context.createUnmarshaller();
        return (T) (Object) unmarchaller.unmarshal(reader);
    }

 
public static ZonedDateTime dateTozoneDateTime(Date d){
    
ZoneId defaultTimeZone = ZoneId.systemDefault();
   //System.out.println(defaultTimeZone);
   Instant instant = d.toInstant();

  ZonedDateTime zonedDate =  ZonedDateTime.ofInstant(instant, defaultTimeZone);
  return zonedDate;
}
}
