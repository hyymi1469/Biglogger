package com.example.BigLogger.src;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class XMLManager {

    // Thread-safe concurrent map
    private final Map<String, LogStruct> structMap = new ConcurrentHashMap<>();

    private XMLManager() {}

    private static class Holder {
        private static final XMLManager INSTANCE = new XMLManager();
    }

    // 싱글톤
    public static XMLManager getInstance() {
        return Holder.INSTANCE;
    }

    public boolean LoadXml() {
        try {
            String filePath = "bigLogger.xml";
            File xmlFile = new File(filePath);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            // 새 xml을 로드할 때 기존 map 비우기
            structMap.clear();

            NodeList structList = doc.getElementsByTagName("struct");

            for (int i = 0; i < structList.getLength(); i++) {
                Element structElem = (Element) structList.item(i);

                LogStruct structData = new LogStruct();
                structData.name = structElem.getAttribute("name");
                structData.desc = structElem.getAttribute("desc");

                String ver = structElem.getAttribute("version");
                if (!ver.isEmpty()) structData.version = Integer.valueOf(ver);

                String filter = structElem.getAttribute("filter");
                if (!filter.isEmpty()) structData.filter = Integer.valueOf(filter);

                NodeList entryNodes = structElem.getElementsByTagName("entry");

                for (int j = 0; j < entryNodes.getLength(); j++) {
                    Element entryElem = (Element) entryNodes.item(j);

                    LogEntry entry = new LogEntry();
                    entry.name = entryElem.getAttribute("name");
                    entry.type = entryElem.getAttribute("type");
                    entry.desc = entryElem.getAttribute("desc");

                    String size = entryElem.getAttribute("size");
                    if (!size.isEmpty()) entry.size = Integer.valueOf(size);

                    String index = entryElem.getAttribute("index");
                    if (!index.isEmpty()) entry.index = Integer.valueOf(index);

                    entry.defaultValue = entryElem.getAttribute("defaultvalue");

                    structData.entries.add(entry);
                }

                structMap.put(structData.name, structData);
            }

            System.out.println("XML Loaded. Struct count = " + structMap.size());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public LogStruct getStruct(String name) {
        return structMap.get(name);
    }

    public Map<String, LogStruct> getStructMap() {
        return structMap;
    }
}