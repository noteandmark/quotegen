package com.andmark.qoutegen.service;

import com.andmark.qoutegen.dto.BookDTO;
import com.kursx.parser.fb2.*;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {

    public List<BookDTO> scanBooks(String directoryPath) {
        File rootDirectory = new File(directoryPath);

        System.out.println("rootDirectory = " + rootDirectory.getAbsolutePath());

        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory path: " + directoryPath);
        }

        List<BookDTO> scannedBooks = new ArrayList<>();
        scanBooksRecursive(rootDirectory, scannedBooks);

        // Get an array of files in the directory
//        File[] files = directory.listFiles();

//        if (files != null) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    System.out.println("");
//                    // Process each file and extract book information
//                    BookDTO bookDTO = processBookFile(file);
//                    if (bookDTO != null) {
//                        scannedBooks.add(bookDTO);
//                    }
//                }
//            }
//        }

        return scannedBooks;
    }

    private void scanBooksRecursive(File directory, List<BookDTO> scannedBooks) {
        System.out.println("in scanBooksRecursive with directory = " + directory.getName());
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    System.out.println("find file : " + file.getName());
                    BookDTO bookDTO = processBookFile(file);
                    if (bookDTO != null) {
                        scannedBooks.add(bookDTO);
                    }
                } else if (file.isDirectory()) {
                    System.out.println("file is directory: " + file.getName());
                    scanBooksRecursive(file, scannedBooks);
                }
            }
        }
    }

    private BookDTO processBookFile(File file) {
        System.out.println("in processBookFile with file: ");

        System.out.println(file.getAbsolutePath());
        System.out.println(file.getName());
        System.out.println(file.getPath());
        try {
            System.out.println(file.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(file.getParent());

        String fileName = file.getName();
        String absolutePath = file.getPath(); // TODO: записать это как адрес книги
        System.out.println("File path : " + absolutePath);

        // Check if the file has a .fb2 extension (you may adapt this for other formats)
        if (!fileName.toLowerCase().endsWith(".fb2")) {
            System.out.println("not fb2, return null");
            return null;
        }

        try {
            System.out.println("in block try");
            FictionBook fb2 = new FictionBook(new File(file.getAbsolutePath()));

            String title = fb2.getTitle();//TODO: записать это как имя книги
            Body body = fb2.getBody();

            System.out.println("title = " + title);

            for (Section section : body.getSections()) {
                System.out.println("section :");
                System.out.println(section);

                ArrayList<Element> elements = section.getElements();
                for (Element element : elements) {
                    System.out.println("element:");
                    System.out.println(element.getText());
                }

                ArrayList<Section> section1 = section.getSections();
                for (Section innerSection1 : section1) {
                    System.out.println("innersection1");
                    System.out.println(innerSection1);

                    ArrayList<Element> innerSectionElements1 = innerSection1.getElements();
                    for (Element innerSectionElement : innerSectionElements1) {
                        System.out.println("innerSectionElement:");
                        System.out.println(innerSectionElement.getText());
                    }

                    ArrayList<Section> section2 = innerSection1.getSections();
                    for (Section innerSection2 : section2) {
                        System.out.println("innersection2");
                        System.out.println(innerSection2);

                        ArrayList<Element> innerSectionElements2 = innerSection2.getElements();
                        for (Element innerSectionElement2 : innerSectionElements2) {
                            System.out.println("innerSectionElement2:");
                            System.out.println(innerSectionElement2.getText());
                        }

                    }


                }


            }



        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }


        // You'll need to handle different formats (fb2, epub, doc) here
        // Create a BookDTO object with the extracted information and return it
        // If the file is not a valid book file, return null
        // You may use external libraries or APIs to handle the parsing of different formats
        return null;
    }

}
