package com.m2i.pti.service.easyBatch;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ReaderFactureService {
        private FactureAssembler assembler;

    private final Logger log = LoggerFactory.getLogger(ReaderFactureService.class);

    public List<CSVFacture>  read(InputStream file) {

        assembler = new FactureAssembler();
        List<CSVFacture> listChargement = new ArrayList();
        //  File file = new File("data.csv");
        try {
            CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(new InputStreamReader(file, StandardCharsets.UTF_8))
                    .strategy(new CSVStrategy(';', '\u0000', '\u0000', false, true))
                    .entryParser(new DefaultCSVEntryParser())
                    .build();
            List<String[]> rows = csvReader.readAll();


            //String array converts to the List of Car.
            rows.stream().forEach(row -> listChargement.add(assembler.assemble(row)));
            //print all Car objects

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listChargement;
    }


    public <T extends Object> T read(InputStream file, CsvAssembler csvAssembler, String pattern) {

        assembler = new FactureAssembler();
        List<CsvData> listChargement = new ArrayList();
        T result = null ;
        //  File file = new File("data.csv");
        try {
            CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(new InputStreamReader(file))
                    .strategy(new CSVStrategy(';', '\u0000', '\u0000', false, true))
                    .entryParser(new DefaultCSVEntryParser())
                    .build();
            List<String[]> rows = csvReader.readAll();


            //String array converts to the List of Car.
            rows.stream().forEach(row -> addToTab(listChargement,row,csvAssembler,pattern));
            //rows.stream().forEach(row -> listChargement.add(csvAssembler.assemble(row)));
            //print all Car objects

            result = (T) listChargement ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private void addToTab( List<CsvData>  listChargement, String[] row,CsvAssembler csvAssembler,String pattern) {
        try {
            Pattern p = Pattern.compile(pattern);
            String str = String.join(";", row);
            String r=row.toString();
            //  Matcher m = p.matcher(str);
            //       boolean bool = m.matches();
            boolean bool = true;
            if (bool==false){
                throw new RuntimeException("Fichier incorrect!!!");
            }else{
                listChargement.add(csvAssembler.assemble(row));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public File convert(MultipartFile file) throws IOException
    {

        File convFile = new File("./IN_FILE/"+file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        //  BasicFileAttributes attr = Files.readAttributes(convFile.toPath(),BasicFileAttributes.class);

        return convFile ;
    }
}
