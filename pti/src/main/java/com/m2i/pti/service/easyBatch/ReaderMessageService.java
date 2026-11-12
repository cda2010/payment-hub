package com.m2i.pti.service.easyBatch;

import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;




import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ReaderMessageService {
    private MessageAssembleur assembler;
    private final Logger log= LoggerFactory.getLogger(ReaderMessageService.class);

    public List<CsvMessage> read(InputStream file) throws IOException {
        assembler =  new MessageAssembleur();
        List<CsvMessage> listChargement = new ArrayList();
        try {
            CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(new InputStreamReader(file,StandardCharsets.UTF_8))
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

    private void addToTab( List<CsvData>  listChargement, String[] row,CsvAssembler csvMessage,String pattern) {
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
                listChargement.add(csvMessage.assemble(row));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public <T extends Object> T read(InputStream file,CsvAssembler csvMessage,String pattern) {

        assembler = new MessageAssembleur();
        List<CsvData> listChargement = new ArrayList();
        T result = null ;
        //  File file = new File("data.csv");
        try {

            CSVReader<String[]> csvReader = new CSVReaderBuilder<String[]>(new InputStreamReader(file,StandardCharsets.UTF_8))

                    .strategy(new CSVStrategy(';', '\u0000', '\u0000', false, true))
                    .entryParser(new DefaultCSVEntryParser())
                    .build();
            List<String[]> rows = csvReader.readAll();


            //String array converts to the List of Car.
            rows.stream().forEach(row -> addToTab(listChargement,row,csvMessage,pattern));
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
    public String hashFile (InputStream file){
        StringBuilder sb = new StringBuilder();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            while ((bytesCount = file.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            file.close();
            byte[] bytes = digest.digest();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return    sb.toString();
    }
}
