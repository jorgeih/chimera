package de.uni_potsdam.hpi.bpt.bp2014.jcore;

import de.uni_potsdam.hpi.bpt.bp2014.database.DbActivityInstance;
import de.uni_potsdam.hpi.bpt.bp2014.database.DbControlNode;

import java.util.HashMap;
import java.util.LinkedList;


/**
 * ********************************************************************************
 * <p/>
 * _________ _______  _        _______ _________ _        _______
 * \__    _/(  ____ \( (    /|(  ____ \\__   __/( (    /|(  ____ \
 * )  (  | (    \/|  \  ( || (    \/   ) (   |  \  ( || (    \/
 * |  |  | (__    |   \ | || |         | |   |   \ | || (__
 * |  |  |  __)   | (\ \) || | ____    | |   | (\ \) ||  __)
 * |  |  | (      | | \   || | \_  )   | |   | | \   || (
 * |\_)  )  | (____/\| )  \  || (___) |___) (___| )  \  || (____/\
 * (____/   (_______/|/    )_)(_______)\_______/|/    )_)(_______/
 * <p/>
 * ******************************************************************
 * <p/>
 * Copyright © All Rights Reserved 2014 - 2015
 * <p/>
 * Please be aware of the License. You may found it in the root directory.
 * <p/>
 * **********************************************************************************
 */


public class HistoryService {
    /**
     * Database Connection objects
     */
    private DbActivityInstance dbActivityInstance = new DbActivityInstance();
    private DbControlNode dbControlNode = new DbControlNode();

    /**
     * Gives all ids of terminated activities for a scenario instance id.
     *
     * @param scenarioInstance_id This is the id of the scenario instance.
     * @return a list of int ids of the activities.
     */
    public LinkedList<Integer> getTerminatedActivitiesForScenarioInstance(int scenarioInstance_id) {
        LinkedList<Integer> ids = dbActivityInstance.getTerminatedActivitiesForScenarioInstance(scenarioInstance_id);
        return ids;
    }

    /**
     * Returns the Labels of terminated activities for a scenario instance id.
     *
     * @param scenarioInstance_id This is the id of the scenario instance.
     * @return a Map. Keys are the activity ids. Values are the labels of the activities.
     */
    public HashMap<Integer, String> getTerminatedActivityLabelsForScenarioInstance(int scenarioInstance_id) {
        LinkedList<Integer> ids = dbActivityInstance.getTerminatedActivitiesForScenarioInstance(scenarioInstance_id);
        HashMap<Integer, String> labels = new HashMap<Integer, String>();
        for (int id : ids) {
            labels.put(id, dbControlNode.getLabel(id));
        }
        return labels;
    }
}
