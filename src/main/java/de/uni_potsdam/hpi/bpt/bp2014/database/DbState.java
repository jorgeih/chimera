package de.uni_potsdam.hpi.bpt.bp2014.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

/**
 * Creates and executes a sql statement to get the name of a corresponding state ID
 */
public class DbState extends DbObject {
    /**
     * This creates and executes a sql statement to get the state name of a dataObject
     * @param id This is the database ID of a state
     * @return the name of the state as a String
     */
    public String getStateName(int id) {
        String sql = "SELECT name FROM state WHERE id = " + id;
        return this.executeStatementReturnsString(sql, "name");
    }
}
