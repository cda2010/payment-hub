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
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReaderCompteService {
    private CompteAssembleur assembler;
    private final Logger log = LoggerFactory.getLogger(ReaderCompteService.class);
    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};

    public List<CsvCompte> read(InputStream file) {

        assembler = new CompteAssembleur();
        List<CsvCompte> listChargement = new ArrayList();
        //  File file = new File("data.csv");
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


    private void addToTab(List<CsvData>  listChargement, String[] row, CsvAssembler csvAssembler, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern);
            String str = String.join(";", row);
            String r=row.toString();
            String b=row[3];
            System.out.println("row------"+b);
            System.out.println("row------"+row[7]);
            Matcher m = p.matcher(str);
          // boolean bool = m.matches();
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


    public <T extends Object> T read(InputStream file,CsvAssembler csvAssembler,String pattern) {

        assembler = new CompteAssembleur();
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

    public Map<String, byte[]> getSignature(byte[] fileContent ) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        Map<String,  byte[]> mapSign = new HashMap();

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            // Get an instance of Signature object and initialize it.
            Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
            signature.initSign(privateKey);
            signature.update(fileContent);
            byte[] digitalSignature = signature.sign();
            mapSign.put("signature", digitalSignature);
            mapSign.put("publickey", keyPair.getPublic().getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapSign;
    }
    public boolean  VerifyDigitalSignature  (byte[] publicKeyEncoded, byte[] digitalSignature,byte[] fileContent) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException, SignatureException{
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyEncoded);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
        signature.initVerify(publicKey);
        signature.update(fileContent);
        boolean verified= signature.verify(digitalSignature);

        return verified;
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
