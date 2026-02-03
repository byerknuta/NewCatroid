package org.catrobat.catroid.content;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.catrobat.catroid.ProjectManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProjectRecoveryManager {

    public static class LostProjectInfo {
        public String folderName;
        public String projectName;
        public File path;

        public LostProjectInfo(String folderName, String projectName, File path) {
            this.folderName = folderName;
            this.projectName = projectName;
            this.path = path;
        }
    }

    public static List<LostProjectInfo> scanForLostProjects(Context context) {
        List<LostProjectInfo> foundProjects = new ArrayList<>();
        File rootDir = context.getFilesDir();

        File[] files = rootDir.listFiles();
        if (files == null) return foundProjects;

        for (File file : files) {
            if (file.isDirectory()) {

                File codeXml = new File(file, "code.xml");
                if (codeXml.exists()) {

                    String realName = getProjectNameFromXml(codeXml);
                    if (realName == null) realName = file.getName();




                    foundProjects.add(new LostProjectInfo(file.getName(), realName, file));
                }
            }
        }
        return foundProjects;
    }

    private static String getProjectNameFromXml(File xmlFile) {
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new InputStreamReader(fis));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {

                    if ("programName".equals(parser.getName())) {
                        return parser.nextText();
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("Recovery", "Error parsing xml: " + xmlFile.getName());
        }
        return null;
    }
}