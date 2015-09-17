package com.testingtech.car2x.hmi.testcases;

import com.testingtech.car2x.hmi.Globals;
import com.testingtech.car2x.hmi.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlLoader {

  private Document document = null;
  private static List<TestCaseGroup> groups;
  private static XmlLoader instance;

    public static XmlLoader getInstance(){
        if(XmlLoader.instance == null)
            XmlLoader.instance = new XmlLoader();
        return XmlLoader.instance;
    }

  private XmlLoader(){
      File path = Globals.mainActivity.getExternalFilesDir(null);
      //File path = Globals.mainActivity.getFilesDir();
      File xml = new File(path, "source.xml");
      try {
          // parse an XML document into a DOM tree
          DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          document = parser.parse(new FileInputStream(xml));
      }catch(IOException | SAXException | ParserConfigurationException e){
          Logger.writeLog("XML document cannot be loaded: " + e.getMessage());
      }
      groups = new ArrayList();
      storeFromSource();
  }

  public void storeFromSource() {
    if(document != null) {
        // handle groups
        NodeList groupNodes = document.getElementsByTagName("group");
        for (int iterator = 0; iterator < groupNodes.getLength(); iterator++) {
            Node currentGroup = groupNodes.item(iterator);
            String groupName = currentGroup.getAttributes().getNamedItem("name").getNodeValue();
            // create group and set name
            TestCaseGroup group = new TestCaseGroup(groupName);

            // handle test cases
            NodeList groupChildNodes = currentGroup.getChildNodes();
            for (int iterator2 = 0; iterator2 < groupChildNodes.getLength(); iterator2++) {
                Node currentChild = groupChildNodes.item(iterator2);
                if (currentChild.getNodeName().equalsIgnoreCase("testCase")) { // to omit text nodes
                    String testCaseId = currentChild.getAttributes().getNamedItem("id").getNodeValue();
                    // create test case and set test case id
                    TestCase testCase = new TestCase(testCaseId);

                    // handle test case title and stages
                    NodeList testCaseChildNodes = currentChild.getChildNodes();
                    for (int iterator3 = 0; iterator3 < testCaseChildNodes.getLength(); iterator3++) {
                        Node currentTestCaseChild = testCaseChildNodes.item(iterator3);
                        if (currentTestCaseChild.getNodeName().equalsIgnoreCase("title")) {
                            String testCaseTitle = currentTestCaseChild.getTextContent();
                            // set test case title
                            testCase.setTitle(testCaseTitle);
                        } else if (currentTestCaseChild.getNodeName().equalsIgnoreCase("stageLabel")) {
                            int stageId = Integer.parseInt(
                                    currentTestCaseChild.getAttributes().getNamedItem("stageId").getNodeValue()
                            );
                            String stageLabel = currentTestCaseChild.getTextContent();
                            // create test case stage and set stage id and label
                            TestCaseStage testCaseStage = new TestCaseStage(stageId, stageLabel);
                            // add stage to test case
                            testCase.addStage(testCaseStage);
                        }
                    }
                    // add test case to group
                    group.addTestCase(testCase);
                }
            }
            // add group to list
            groups.add(group);
        }
    }else{
        Logger.writeLog("Groups cannot be created.");
    }
  }

  public static List<TestCaseGroup> getTestCaseGroups() {
    return groups;
  }

    public static List<String> getTestCaseGroupNames(){
        ArrayList<String> names = new ArrayList<>(groups.size());
        for(TestCaseGroup group : groups){
            names.add(group.getName());
        }
        return names;
    }

    public static TestCase getTestCaseById(String testCaseId){
        for (TestCaseGroup group : groups){
            for (TestCase testCase : group.getTestCases()){
                if(testCase.getId().equals(testCaseId))
                    return testCase;
            }
        }
        return null;
    }

  /**
   * Returns the id of a test case based on a target group and underlying test case.
   * The group and test case are selected by their positions in the list. Group positions start
   * from 0 onwards. Test case positions start from 0 onwards per group.
   *
   * Example:
   * groupA
   *    testcaseX
   *    testcaseY
   * groupB
   *    testcaseZ
   * To obtain the id of testcaseZ, pass groupPostion 1 and testCasePosition 0.
   *
   * @param groupPosition Position of the group.
   * @param testCasePosition Position of the test case inside the group.
   * @return The group Id or null on invalid parameters or unset testCaseId value.
   */
  public static String getTestCaseId(int groupPosition, int testCasePosition) {
    try {
      TestCaseGroup targetGroup = groups.get(groupPosition);
      List<TestCase> testCases = targetGroup.getTestCases();
      TestCase testCase = testCases.get(testCasePosition);
      return testCase.getId();
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace(Logger.writer);
      return null;
    }
  }
}
